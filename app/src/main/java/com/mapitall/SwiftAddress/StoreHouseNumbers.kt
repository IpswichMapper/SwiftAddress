package com.mapitall.SwiftAddress

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.provider.Contacts.SettingsColumns.KEY
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.ConfigurationCompat
import de.westnordost.osmapi.OsmConnection
import kotlinx.parcelize.Parcelize
import oauth.signpost.OAuthProvider
import oauth.signpost.basic.DefaultOAuthConsumer
import oauth.signpost.basic.DefaultOAuthProvider
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.collections.HashMap

// AddressNodes Class which conveniently packages all the information about
// an address object into one data class that can be shared accross activities.
@Parcelize
data class AddressNodes(
        var housenumber: String,
        val street: String = "",
        var latitude: Double,
        var longitude: Double,
        val side: String,
        var buildingLevels: String,
        var houseName: String = "") : Parcelable

class StoreHouseNumbers(private val context: Context) : SQLiteOpenHelper(context,
        "address_database",
        null,
        3) {
    private val TAG = "StoreHouseNumbers"

    // All the database table columns.
    private val TABLE_NAME = "address_database"
    private val TEMP_TABLE_NAME = "saved_address_database"
    private val WAYS_TABLE_NAME = "interpolation_ways_table"
    private val WAYS_TEMP_TABLE_NAME = "saved_interpolation_ways_table"

    private val COL_ID = "ID"
    private val COL_HOUSENUMBER = "HOUSENUMBER"
    private val COL_STREET = "STREET"
    private val COL_LATITUDE = "LATITUDE"
    private val COL_LONGITUDE = "LONGITUDE"
    private val COL_NOTE = "NOTE"
    private val COL_SIDE = "SIDE"
    private val COL_BUILDING_LEVELS = "BUILDING_LEVELS"
    private val COL_HOUSENAME = "HOUSENAME"
    private val COL_TYPE = "TYPE"
    private val COL_REF = "INTERPOLATION_WAY"

    private val WAY_COL_INTERPOLATION = "INTERPOLATION"
    private val WAY_COL_INCLUSION = "INCLUSION"
    private val WAY_COL_START_MARKER_ID = "START_MARKER_ID"
    private val WAY_COL_END_MARKER_ID = "END_MARKER_ID"

    // Write housenumbers to .osm file and notes to .osc file.
    fun writeToOsmFile() : Pair<Int, Int> {

        val (textFilePairs, countPairs) = databaseToXml()
        val (addressTextToWrite, noteTextToWrite) = textFilePairs
        val (i, j) = countPairs

        var isExternalStorageReadOnly: Boolean = false
        val extStorageState = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState) {
            isExternalStorageReadOnly = true
        }
        Log.i("ExtStorageReadOnly", "$isExternalStorageReadOnly")
        var isExternalStorageAvailable: Boolean = false
        if (Environment.MEDIA_MOUNTED == extStorageState) {
            isExternalStorageAvailable = true
        }

        Log.w("addressFile", addressTextToWrite)
        Log.w("noteFile", noteTextToWrite)
        if (isExternalStorageAvailable && !isExternalStorageReadOnly) {

            val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)
            val date = SimpleDateFormat("dd-mm-yyyy-hh-mm-ss", locale).format(Date())
            val addressFileName = "housenumbers-$date.osm"
            val noteFileName = "notes-$date.osc"

            Log.i("ExtStorageAvailable", "$isExternalStorageAvailable")


            val folderPath = context.getExternalFilesDir("data")!!
                    .absolutePath + File.separator

            if (!File(folderPath).exists()) {
                File(folderPath).mkdir()
            }


            val addressFile = File(folderPath + addressFileName)
            val addressFileOutputStream: FileOutputStream


            val noteFile = File(folderPath + noteFileName)
            val noteFileOutputStream: FileOutputStream
            try {

                if (addressTextToWrite != "") {
                    addressFile.createNewFile()
                    addressFileOutputStream = FileOutputStream(addressFile, false)
                    addressFileOutputStream.write(addressTextToWrite.toByteArray())
                    addressFileOutputStream.flush()
                    addressFileOutputStream.close()

                    Log.i(TAG, "addressFile written to Internal Storage")
                }
                if (noteTextToWrite != "") {
                    noteFile.createNewFile()
                    noteFileOutputStream = FileOutputStream(noteFile, false)
                    noteFileOutputStream.write(noteTextToWrite.toByteArray())
                    noteFileOutputStream.flush()
                    noteFileOutputStream.close()

                    Log.i(TAG, "noteFile written to Internal Storage")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return Pair(i, j)
    }

    // This converts a "AddressNodes" object to OSM-XML format (.osm)
    // Converts a note to OsmChange format (.osc)
    private fun databaseToXml(): Pair<Pair<String, String>, Pair<Int, Int>> {

        // NOTE: THE GENERATOR USED TO BE CALLED "KEYPAD MAPPER 4"
        // Make sure to search for that when looking for related changesets.
        val addressFileStart = """<?xml version="1.0" encoding="UTF-8"?>
        <osm version="0.6" generator="SwiftAddress">
        
        """.trimIndent()
        val addressFileEnd = "</osm>"
        val addressFileToWrite = StringBuilder()
        val noteFileToWrite = StringBuilder()

        val noteFileStart = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <osmChange generator="SwiftAddress" version="0.6">
             <create>

        """.trimIndent()
        val noteFileEnd = """
            </create>
            </osmChange>
        """.trimIndent()

        addressFileToWrite.append(addressFileStart)
        noteFileToWrite.append(noteFileStart)

        val db: SQLiteDatabase = this.readableDatabase
        val addressFileMiddle = StringBuilder()
        val noteFileMiddle = StringBuilder()

        var c: Cursor = db.query(TABLE_NAME, null, null, null,
                null, null, "ID ASC")


        var i = -1
        var j = -1
        // Key: node ID in XML file
        // Value: interpolation way ID
        val nodeHashMap = HashMap<Int, Int>()

        // Key: ID of address from database
        // Value: ID of the address in XML file
        val addressIDHashMap = HashMap<Int, Int>()
        while (c.moveToNext()) {
            val type = c.getString(c.getColumnIndex(COL_TYPE))

            if (type == "Address") {
                val housenumber = c.getString(c.getColumnIndex(COL_HOUSENUMBER))
                val street = c.getString(c.getColumnIndex(COL_STREET))
                val latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))
                val buildingLevels = c.getString(c.getColumnIndex(COL_BUILDING_LEVELS))
                val housename = c.getString(c.getColumnIndex(COL_HOUSENAME))
                val ID = c.getInt(c.getColumnIndex(COL_ID))

                addressFileMiddle.append("<node id=\"$i\" lat=\"$latitude\" lon=\"$longitude\">\n") // opening tag
                addressFileMiddle.append("<tag k=\"addr:housenumber\" v=\"$housenumber\"/>\n") // housenumber
                if (street != "") {
                    addressFileMiddle.append("<tag k=\"addr:street\" v=\"$street\"/>\n") // street
                }
                if (housename != "") {
                    addressFileMiddle.append("<tag k=\"addr:housename\" v=\"$housename\"/>\n") //housename
                }
                if (buildingLevels != "") {
                    val parts = buildingLevels.split(" ")

                    var buildingLevelsNum = parts[0]
                    buildingLevelsNum = buildingLevelsNum.drop(1)
                    var roofLevelsNum = parts[1]
                    roofLevelsNum = roofLevelsNum.drop(1)

                    addressFileMiddle.append(
                            "<tag k=\"building:levels\" v=\"$buildingLevelsNum\"/>\n")
                    addressFileMiddle.append(
                            "<tag k=\"roof:levels\" v=\"$roofLevelsNum\"/>\n")
                }
                addressFileMiddle.append("<tag k=\"source:addr\" v=\"SwiftAddress\"/>\n")
                addressFileMiddle.append("</node>\n")
                addressIDHashMap[ID] = i
                i--
            } else if (type == "Note") {
                val contents = c.getString(c.getColumnIndex(COL_NOTE))
                val latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))

                noteFileMiddle.append("<note id=\"$j\" lat=\"$latitude\" lon=\"$longitude\">\n")
                noteFileMiddle.append("<comment text=\"$contents\" />\n")
                noteFileMiddle.append("</note>\n")
                j--
            } else if (type == "Node") {
                val latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))

                nodeHashMap[i] = c.getInt(c.getColumnIndex(COL_REF))
                addressFileMiddle.append(
                        "<node id=\"$i\" lat=\"$latitude\" lon=\"$longitude\"/> \n")
                i--
            } else {

                Log.i(TAG, "data type wasn't note, node or address. " +
                        "It was mostly likely an image")
            }
        }
        c.close()

        c = db.rawQuery("SELECT * FROM $WAYS_TABLE_NAME;", null)
        while (c.moveToNext()) {
            val interpolation = c.getString(c.getColumnIndex(WAY_COL_INTERPOLATION))
            val inclusion = c.getString(c.getColumnIndex(WAY_COL_INCLUSION))
            val startMarkerID = c.getInt(c.getColumnIndex(WAY_COL_START_MARKER_ID))
            val endMarkerID = c.getInt(c.getColumnIndex(WAY_COL_END_MARKER_ID))
            val ID = c.getInt(c.getColumnIndex(COL_ID))
            addressFileMiddle.append("<way id=\"$i\">\n")

            addressFileMiddle.append("<nd ref=\"${addressIDHashMap[startMarkerID]}\" />\n")
            for (node in nodeHashMap) {
                if (node.value == ID) addressFileMiddle.append("<nd ref=\"${node.key}\" />\n")
            }
            addressFileMiddle.append("<nd ref=\"${addressIDHashMap[endMarkerID]}\" />\n")
            if (interpolation != "")
                addressFileMiddle.append("<tag k=\"addr:interpolation\" v=\"$interpolation\" />\n")
            if (inclusion != "")
                addressFileMiddle.append("<tag k=\"addr:inclusion\" v=\"$inclusion\" />\n")
            addressFileMiddle.append("</way>\n")
            i--
        }
        c.close()

        addressFileToWrite.append(addressFileMiddle)
        addressFileToWrite.append(addressFileEnd)

        noteFileToWrite.append(noteFileMiddle)
        noteFileToWrite.append(noteFileEnd)
        if (addressFileMiddle.toString() != "" && noteFileMiddle.toString() == "") {
            Log.i(TAG, "Writing Addresses to File")
            return Pair(Pair(addressFileToWrite.toString(), ""), Pair(i, j))
        } else if (addressFileMiddle.toString() != "" && noteFileMiddle.toString() != "") {
            Log.i(TAG, "Writing Addresses and Notes to File")
            return Pair(Pair(addressFileToWrite.toString(), noteFileToWrite.toString()), Pair(i, j))
        } else if (addressFileMiddle.toString() == "" && noteFileMiddle.toString() != "") {
            Log.i(TAG, "Writing Notes to File")
            return Pair(Pair("", noteFileToWrite.toString()), Pair(i, j))
        } else {
            Log.i(TAG, "Writing Nothing to File")
            return Pair(Pair("", ""), Pair(i, j))
        }
    }


    fun zipFilesAndDelete(zipUri : Uri) : Int {
        val buffer = 2048

        var k = 0
        val surveyFolder = File(context.getExternalFilesDir("data")!!.absolutePath)
        val surveyFiles: Array<File>? = surveyFolder.listFiles()

        if (surveyFiles != null) {
            val zipOutputStream = ZipOutputStream(
                    context.contentResolver.openOutputStream(zipUri)
            )


            for (file in surveyFiles) {

                val bufferByteArray = ByteArray(buffer)
                val fileInputStream = FileInputStream(file)
                zipOutputStream.putNextEntry(ZipEntry(file.name))

                var length: Int = fileInputStream.read(bufferByteArray)
                while (length > 0) {
                    zipOutputStream.write(bufferByteArray, 0, length)
                    length = fileInputStream.read(bufferByteArray)
                }
                zipOutputStream.closeEntry()
                fileInputStream.close()
                if (file.extension == "jpg") {
                    k++
                }

            }
            zipOutputStream.close()
            Log.i(TAG, "zipFilesAndDelete: files zipped")
            for (file in surveyFiles) {
                file.delete()
            }
            Log.i(TAG, "zipFilesAndDelete: files deleted")

        }
        return k
    }

    // Creates database to store the Addresses and Notes
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE $TABLE_NAME(ID INTEGER PRIMARY KEY AUTOINCREMENT,
           |TYPE TEXT,
           |HOUSENUMBER TEXT,
           |STREET TEXT,
           |LATITUDE REAL NOT NULL,
           |LONGITUDE REAL NOT NULL,
           |SIDE TEXT,
           |BUILDING_LEVELS TEXT,
           |HOUSENAME TEXT,
           |NOTE TEXT,
           |INTERPOLATION_WAY INTEGER);""".trimMargin())

        db.execSQL("""CREATE TABLE $WAYS_TABLE_NAME(ID INTEGER PRIMARY KEY AUTOINCREMENT,
            |INTERPOLATION TEXT,
            |INCLUSION TEXT,
            |START_MARKER_ID INTEGER,
            |END_MARKER_ID INTEGER);""".trimMargin())

        db.execSQL("CREATE TABLE $TEMP_TABLE_NAME AS SELECT * FROM $TABLE_NAME")
        db.execSQL("CREATE TABLE $WAYS_TEMP_TABLE_NAME AS SELECT * FROM $WAYS_TABLE_NAME")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TEMP_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $WAYS_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $WAYS_TEMP_TABLE_NAME")
        onCreate(db)
    }

    // Remove all rows from the main table, and move them to a "temporary table" (so that they
    // can be recovered later if need be)
    fun clearDatabase() {
        val db: SQLiteDatabase = this.writableDatabase
        var doNotExecuteNext = false
        var doNotExecuteNextWays = false
        try {
            db.delete(TEMP_TABLE_NAME, null, null)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            db.execSQL("CREATE TABLE $TEMP_TABLE_NAME AS SELECT * FROM $TABLE_NAME")
            doNotExecuteNext = true
        }
        try {
            db.delete(WAYS_TEMP_TABLE_NAME, null, null)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            db.execSQL("CREATE TABLE $WAYS_TEMP_TABLE_NAME AS SELECT * FROM $TABLE_NAME")
            doNotExecuteNextWays = true
        }
        if (!doNotExecuteNext) {
            db.execSQL("INSERT INTO $TEMP_TABLE_NAME SELECT * FROM $TABLE_NAME")
        }
        if (!doNotExecuteNextWays) {
            db.execSQL("INSERT INTO $WAYS_TEMP_TABLE_NAME SELECT * FROM $WAYS_TABLE_NAME")
        }
        db.delete(TABLE_NAME, null, null)
        db.delete(WAYS_TABLE_NAME, null, null)
        db.close()
        Log.i(TAG, "Database cleared.")
    }

    /*
    fun clearDatabaseToTrash() {
        val db: SQLiteDatabase = this.writableDatabase
    }
    */

    // Adds housenumber to database once it is created.
    fun addHouseNumber(address: AddressNodes): Int {

        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COL_HOUSENUMBER, address.housenumber)
        contentValues.put(COL_STREET, address.street)
        contentValues.put(COL_LATITUDE, address.latitude)
        contentValues.put(COL_LONGITUDE, address.longitude)
        contentValues.put(COL_SIDE, address.side)
        contentValues.put(COL_TYPE, "Address")
        contentValues.put(COL_BUILDING_LEVELS, address.buildingLevels)
        contentValues.put(COL_HOUSENAME, address.houseName)

        val result: Long = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        Log.i(TAG, "result: $result")
        return result.toInt()

    }

    // Adds notes to database once it is created.
    fun addNote(noteContents: String, lat: Double, lon: Double): Int {

        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COL_NOTE, noteContents)
        contentValues.put(COL_LATITUDE, lat)
        contentValues.put(COL_LONGITUDE, lon)
        contentValues.put(COL_TYPE, "Note")

        val result: Long = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return result.toInt()
    }

    // Add an row containing information about an image stored in internal storage
    @SuppressLint("Recycle")
    fun addImage(absolutePath: String, lat: Double, lon: Double): Int {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COL_NOTE, absolutePath)
        contentValues.put(COL_TYPE, "Image")
        contentValues.put(COL_LATITUDE, lat)
        contentValues.put(COL_LONGITUDE, lon)

        val result: Long = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return result.toInt()
    }

    // Return the item type of the last item that was added.
    fun lastItemType(): String {
        val db: SQLiteDatabase = this.readableDatabase
        val c: Cursor = db.query(TABLE_NAME, null, null, null,
                null, null, "ID DESC")
        c.moveToNext()
        val itemType = c.getString(c.getColumnIndex(COL_TYPE))
        Log.i(TAG, "Item Type: $itemType")
        c.close()
        db.close()
        return itemType
    }

    fun getItemType(markerID: Int): String {
        val db: SQLiteDatabase = this.readableDatabase

        val row = db.rawQuery(
                    "SELECT * FROM $TABLE_NAME WHERE ID = $markerID", null)
        row.moveToFirst()
        val itemType = row.getString(row.getColumnIndex(COL_TYPE))
        row.close()
        return itemType
    }

    fun getHouseNumber(markerID: Int): String {
        val db: SQLiteDatabase = this.readableDatabase

        val row = db.rawQuery(
                    "SELECT * FROM $TABLE_NAME WHERE ID = $markerID", null)
        row.moveToFirst()
        val houseNumber = row.getString(row.getColumnIndex(COL_HOUSENUMBER))
        row.close()
        db.close()
        return houseNumber
    }

    // removes the last object from database.
    fun undo(isAnImage: Boolean) {
        val db: SQLiteDatabase = this.writableDatabase

        if (isAnImage) {
            val c: Cursor = db.query(TABLE_NAME, null, null, null,
                    null, null, "ID DESC")
            c.moveToNext()
            val absolutePath = c.getString(c.getColumnIndex(COL_NOTE))
            File(absolutePath).delete()
            Log.i(TAG, "Item Type: ${c.getString(1)}")
            c.close()
        }

        db.execSQL("DELETE FROM $TABLE_NAME WHERE ID = (SELECT MAX(ID) FROM $TABLE_NAME);")
        db.close()
    }


    // Displays Markers from Database on the map when app is launched.

    fun displayMarkers(mapClass: Map, mainActivity: MainActivity) {
        val db: SQLiteDatabase = this.readableDatabase
        val markerHashMap = HashMap<Int, Marker>()

        val c: Cursor = db.query(TABLE_NAME, null, null, null,
                null, null, "ID ASC")

        while (c.moveToNext()) {
            if (c.getString(c.getColumnIndex(COL_TYPE)) == "Address") {

                val housenumber = c.getString(c.getColumnIndex(COL_HOUSENUMBER))
                val street = c.getString(c.getColumnIndex(COL_STREET))
                val housename = c.getString(c.getColumnIndex(COL_HOUSENAME))
                val latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))
                val id = c.getInt(c.getColumnIndex(COL_ID))

                markerHashMap[id] = Marker(mapClass.mapView)
                markerHashMap.getValue(id).position = GeoPoint(latitude, longitude)

                val drawable = ContextCompat.getDrawable(context, R.drawable.address)!!
                if (housenumber.length <= 5) {
                    val bm = drawable.toBitmap()


                    val paint = Paint()
                    paint.style = Paint.Style.FILL
                    paint.color = Color.WHITE
                    if (housenumber.length <= 3) {
                        paint.textSize = 40f
                    } else if (housenumber.length == 4) {
                        paint.textSize = 30f
                    } else {
                        paint.textSize = 25f
                    }
                    paint.textAlign = Paint.Align.CENTER

                    val canvas = Canvas(bm)

                    // https://stackoverflow.com/a/11121873
                    // explanation of this line
                    canvas.drawText(housenumber, bm.width / 2f,
                            bm.height / 2f - (paint.descent() + paint.ascent() / 2), paint)

                    val icon = BitmapDrawable(context.resources, bm)
                    markerHashMap.getValue(id).icon = icon
                } else {
                    markerHashMap.getValue(id).icon = drawable
                }
                markerHashMap.getValue(id).setAnchor(
                        Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

                val infoWindow = AddressMarkerWindow(
                        R.layout.address_press_layout_linear,
                        mapClass,
                        context,
                        id,
                        mainActivity,
                        housenumber,
                        street,
                        housename)

                markerHashMap.getValue(id).infoWindow = infoWindow
                Log.i(TAG, "Address Marker added")

            } else if (c.getString(c.getColumnIndex(COL_TYPE)) == "Note") {
                val noteContents = c.getString(c.getColumnIndex(COL_NOTE))
                val latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))
                val id = c.getInt(c.getColumnIndex(COL_ID))

                markerHashMap[id] = Marker(mapClass.mapView)
                markerHashMap.getValue(id).position = GeoPoint(latitude, longitude)
                markerHashMap.getValue(id).icon = ContextCompat.getDrawable(context,
                        R.drawable.note)
                markerHashMap.getValue(id).setAnchor(
                        Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                val infoWindow = NoteMarkerWindow(
                    mapClass,
                    context,
                    id,
                    mainActivity,
                    noteContents)
                markerHashMap.getValue(id).infoWindow = infoWindow
                Log.i(TAG, "Note Marker added")

            } else if (c.getString(c.getColumnIndex(COL_TYPE)) == "Image") {
                val latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))
                val id = c.getInt(c.getColumnIndex(COL_ID))
                val imageName = c.getString(c.getColumnIndex(COL_NOTE))

                markerHashMap[id] = Marker(mapClass.mapView)
                markerHashMap.getValue(id).position = GeoPoint(latitude, longitude)
                markerHashMap.getValue(id).icon = ContextCompat.getDrawable(context,
                        R.drawable.camera)
                markerHashMap.getValue(id).setAnchor(
                        Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                markerHashMap.getValue(id).title = imageName
                Log.i(TAG, "Image Marker added")

            } else {
                Log.e(TAG, "Adding a marker failed: data type wasn't a " +
                        "\"Note\", \"Address\" or \"Image\"")
            }
        }
        c.close()
        val c2 = db.query(WAYS_TABLE_NAME, null, null, null,
                null, null, "ID ASC")

        val polyLineHashMap = HashMap<Int, Polyline>()

        while (c2.moveToNext()) {
            val iD = c2.getInt(0)
            val startMarkerID = c2.getInt(c2.getColumnIndex(WAY_COL_START_MARKER_ID))
            val endMarkerID = c2.getInt(c2.getColumnIndex(WAY_COL_END_MARKER_ID))

            val startMarkerRow = db.rawQuery(
                    "SELECT * FROM $TABLE_NAME WHERE ID = $startMarkerID", null)
            val endMarkerRow = db.rawQuery(
                    "SELECT * FROM $TABLE_NAME WHERE ID = $endMarkerID", null)
            startMarkerRow.moveToFirst()
            endMarkerRow.moveToFirst()
            val startMarkerGeoPoint = GeoPoint(
                    startMarkerRow.getDouble(startMarkerRow.getColumnIndex(COL_LATITUDE)),
                    startMarkerRow.getDouble(startMarkerRow.getColumnIndex(COL_LONGITUDE)))
            val endMarkerGeoPoint = GeoPoint(
                    endMarkerRow.getDouble(endMarkerRow.getColumnIndex(COL_LATITUDE)),
                    endMarkerRow.getDouble(endMarkerRow.getColumnIndex(COL_LONGITUDE)))

            startMarkerRow.close()
            endMarkerRow.close()

            polyLineHashMap[iD] = Polyline()
            polyLineHashMap.getValue(iD).addPoint(startMarkerGeoPoint)

            val c3 = db.query(TABLE_NAME, null, null, null,
                    null, null, "ID ASC")

            while (c3.moveToNext()) {
                if (c3.getInt(c3.getColumnIndex(COL_REF)) == iD) {
                    val lat = c3.getDouble(c3.getColumnIndex(COL_LATITUDE))
                    val lon = c3.getDouble(c3.getColumnIndex(COL_LONGITUDE))
                    polyLineHashMap.getValue(iD).addPoint(GeoPoint(lat, lon))

                }
            }
            polyLineHashMap.getValue(iD).addPoint(endMarkerGeoPoint)
            c3.close()
        }
        c2.close()
        mapClass.mapView.invalidate()
        mapClass.setMarkerHashMap(markerHashMap)
        mapClass.setPolylineHashMap(polyLineHashMap)
        db.close()
    }

    // Gets last address that was entered
    fun lastAddressEntry(side: String): AddressNodes? {
        val db: SQLiteDatabase = this.readableDatabase
        var lastAddress: AddressNodes? = null
        val c: Cursor = db.query(TABLE_NAME, null, null, null,
                null, null, "ID DESC")


        var runCondition = true
        while (c.moveToNext() && runCondition) {
            Log.i(TAG, "${c.getString(c.getColumnIndex(COL_TYPE))}, ${
                c.getString(c.getColumnIndex(COL_TYPE)) == "Address"
            }")
            Log.i(TAG, "${c.getString(c.getColumnIndex(COL_SIDE))}, ${
                c.getString(c.getColumnIndex(COL_SIDE)) == side
            }, $side")
            if (c.getString(c.getColumnIndex(COL_TYPE)) == "Address" &&
                    c.getString(c.getColumnIndex(COL_SIDE)) == side) {
                Log.i(TAG, "in if statemant.")
                lastAddress = AddressNodes(
                        c.getString(c.getColumnIndex(COL_HOUSENUMBER)),
                        c.getString(c.getColumnIndex(COL_STREET)),
                        c.getDouble(c.getColumnIndex(COL_LATITUDE)),
                        c.getDouble(c.getColumnIndex(COL_LONGITUDE)),
                        c.getString(c.getColumnIndex(COL_SIDE)),
                        c.getString(c.getColumnIndex(COL_BUILDING_LEVELS)),
                        c.getString(c.getColumnIndex(COL_HOUSENAME))
                )
                runCondition = false
            }
        }

        Log.i(TAG, "lastAddress: ${lastAddress.toString()}")

        c.close()
        return lastAddress

    }

    // Remove marker at specific position.
    // TODO : Check if the function is fixed
    fun removeAt(ID: Int) : Int {

        val db = this.writableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE ID = $ID",
                null)
        c.moveToFirst()
        val itemType = c.getString(c.getColumnIndex(COL_TYPE))
        Log.i(TAG, "Item Type: $itemType")

        // TODO : Check this works when you add image deletion functionality.
        if (itemType == "Image") {
            val absolutePath = c.getString(c.getColumnIndex(COL_NOTE))
            File(absolutePath).delete()
            c.close()
        }

        val result = db.delete(TABLE_NAME, "ID = $ID", null)
        db.close()
        return result
    }


    fun removePolylineAt(id: Int) {
        val db: SQLiteDatabase = this.writableDatabase

        db.delete(TABLE_NAME, "$COL_REF = ?", arrayOf(id.toString()))
        db.delete(WAYS_TABLE_NAME, "ID = ?", arrayOf(id.toString()))

        db.close()

    }

    // Change location of marker after it has been moved.
    fun changeLocation(ID: Int, lat: Double, lon: Double) {
        val db = this.writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET LATITUDE = $lat WHERE ID = $ID")
        db.execSQL("UPDATE $TABLE_NAME SET LONGITUDE = $lon WHERE ID = $ID")
        db.close()
    }

    // Gets old data from a "temporary table", and overwrites current table with
    // rows from "temporary table". The "temporary table" consists of data from
    // the last time the user saved.
    fun recoverData(mapClass: Map, mainActivity: MainActivity) {

        Log.i(TAG, "Attempting to recover data.")
        val db = this.writableDatabase

        val recoverDataDialog = AlertDialog.Builder(context)
        recoverDataDialog.setPositiveButton(context.getString(R.string.recover)) { _, _ ->
            try {
                db.delete(TABLE_NAME, null, null)
                db.delete(WAYS_TABLE_NAME, null, null)
                db.execSQL("INSERT INTO $TABLE_NAME SELECT * FROM $TEMP_TABLE_NAME")
                db.execSQL("INSERT INTO $WAYS_TABLE_NAME SELECT * FROM $WAYS_TEMP_TABLE_NAME")
                val rows = db.delete(TABLE_NAME, "TYPE = 'Image'", null)
                Log.i(TAG, "$rows rows deleted (image rows)")
                mainActivity.restart()

            } catch (e: SQLiteException) {
                e.printStackTrace()

                Toast.makeText(context, context.getString(R.string.no_saved_data),
                        Toast.LENGTH_SHORT).show()
            }
        }

        recoverDataDialog.setNeutralButton(context.getString(R.string.cancel)) { _, _ -> }
        recoverDataDialog.setTitle(context.getString(R.string.recover_data))
        recoverDataDialog.setMessage(context.getString(R.string.recover_data_message))

        recoverDataDialog.create().show()

    }

    // Changes address in the database when it has been modified using an "InfoWindow"
    // that pops up when you click on the marker.
    fun changeAddress(ID: Int, housenumber: String, street: String, houseName: String) {
        val db: SQLiteDatabase = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(COL_HOUSENUMBER, housenumber)
        contentValues.put(COL_STREET, street)
        contentValues.put(COL_HOUSENAME, houseName)
        db.update(TABLE_NAME, contentValues, "ID = ?", arrayOf(ID.toString()))
    }


    fun changeNote(ID: Int, note: String) {
        val db: SQLiteDatabase = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(COL_NOTE, note)
        db.update(TABLE_NAME, contentValues, "ID = ?", arrayOf(ID.toString()))
        db.close()
    }


    fun changePolyline(ID: Int, interpolation: String, inclusion: String) {
        val db: SQLiteDatabase = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(WAY_COL_INTERPOLATION, interpolation)
        contentValues.put(WAY_COL_INCLUSION, inclusion)
        db.update(WAYS_TABLE_NAME, contentValues, "ID = ?", arrayOf(ID.toString()))

        db.close()
    }

    // This adds an "interpolation way" to the database
    fun addPolyline(startMarkerID: Int,
                    geoPoints: MutableList<GeoPoint>,
                    endMarkerID: Int,
                    interpolation: String,
                    inclusion: String) : Int {
        val db = this.writableDatabase
        val wayContentValues = ContentValues()
        wayContentValues.put(WAY_COL_INCLUSION, inclusion)
        wayContentValues.put(WAY_COL_INTERPOLATION, interpolation)
        wayContentValues.put(WAY_COL_START_MARKER_ID, startMarkerID)
        wayContentValues.put(WAY_COL_END_MARKER_ID, endMarkerID)

        val wayRow = db.insert(WAYS_TABLE_NAME, null, wayContentValues)
        geoPoints.removeFirst()
        geoPoints.removeLast()
        for (geoPoint in geoPoints) {
            val contentValues = ContentValues()
            contentValues.put(COL_LATITUDE, geoPoint.latitude)
            contentValues.put(COL_LONGITUDE, geoPoint.longitude)
            contentValues.put(COL_TYPE, "Node")
            contentValues.put(COL_REF, wayRow)

            db.insert(TABLE_NAME, null, contentValues).toInt()
        }
        db.close()

        return wayRow.toInt()
    }

    fun lastPolyLineID(): Int {
        val db = this.readableDatabase
        try {
            val c: Cursor = db.rawQuery(
                    "SELECT * FROM $WAYS_TABLE_NAME WHERE ID = (SELECT MAX(ID) FROM $WAYS_TABLE_NAME);",
                    null)
            c.moveToFirst()
            val id = c.getInt(c.getColumnIndex(COL_ID))
            c.close()
            db.close()
            return id
        } catch (e: SQLiteException) {
            e.printStackTrace()
            db.close()
            return -1
        } catch (e: CursorIndexOutOfBoundsException) {
            Log.w(TAG, "Cursor Index out of range, list is empty")
            db.close()
            return -1
        }

    }

    fun getPolylineDetails(ID: Int): Pair<String, String> {
        val db = this.readableDatabase

        val row = db.rawQuery(
                    "SELECT * FROM $WAYS_TABLE_NAME WHERE ID = $ID", null)
        row.moveToFirst()
        val interpolation = row.getString(row.getColumnIndex(WAY_COL_INTERPOLATION))
        val inclusion = row.getString(row.getColumnIndex(WAY_COL_INCLUSION))
        row.close()
        db.close()
        return Pair(interpolation, inclusion)
    }

    fun getPolylineHouseNumbers(ID: Int): Pair<String, String> {
        val db = this.writableDatabase

        val row = db.rawQuery(
                "SELECT * FROM $WAYS_TABLE_NAME WHERE ID = $ID", null)
        row.moveToFirst()
        val startMarkerID = row.getInt(row.getColumnIndex(WAY_COL_START_MARKER_ID))
        val endMarkerID = row.getInt(row.getColumnIndex(WAY_COL_END_MARKER_ID))

        val startMarkerCursor = db.rawQuery(
                "SELECT * FROM $TABLE_NAME WHERE ID = $startMarkerID", null)
        val endMarkerCursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE ID = $endMarkerID",
                null)
        startMarkerCursor.moveToFirst()
        endMarkerCursor.moveToFirst()
        val startHouseNumber = startMarkerCursor.getString(startMarkerCursor.getColumnIndex(
                COL_HOUSENUMBER))
        val endHouseNumber = endMarkerCursor.getString(endMarkerCursor.getColumnIndex(
                COL_HOUSENUMBER))

        startMarkerCursor.close()
        endMarkerCursor.close()
        row.close()
        db.close()

        return Pair(startHouseNumber, endHouseNumber)
    }

    // This lets you delete any polylines connected to a address marker.
    // It returns the IDs of the polylines deleted so that the Map Class can
    // delete them from the map.
    fun deleteRelatedPolylines(markerID: Int) : List<Int> {
        val db = this.writableDatabase
        val polylineIDs = mutableListOf<Int>()
        val startMarkerCursor = db.rawQuery(
                "SELECT * FROM $WAYS_TABLE_NAME WHERE $WAY_COL_START_MARKER_ID = $markerID",
                null)
        // startMarkerCursor.moveToFirst()
        while (startMarkerCursor.moveToNext()) {
            val polylineID = startMarkerCursor.getInt(startMarkerCursor.getColumnIndex(COL_ID))
            polylineIDs.add(polylineID)
        }
        db.delete(WAYS_TABLE_NAME, "$WAY_COL_START_MARKER_ID = $markerID", null)
        db.close()
        val db2 = this.writableDatabase
        val endMarkerCursor = db2.rawQuery(
                "SELECT * FROM $WAYS_TABLE_NAME WHERE $WAY_COL_END_MARKER_ID = $markerID",
                null)
        // endMarkerCursor.moveToFirst()
        while(endMarkerCursor.moveToNext()) {
            val polylineID = endMarkerCursor.getInt(endMarkerCursor.getColumnIndex(COL_ID))
            polylineIDs.add(polylineID)
        }
        db2.delete(WAYS_TABLE_NAME, "$WAY_COL_END_MARKER_ID = $markerID", null)

        startMarkerCursor.close()
        endMarkerCursor.close()
        db.close()
        return polylineIDs
        /*
        db.delete(WAYS_TABLE_NAME, "$WAY_COL_START_MARKER_ID = $markerID",null)
        db.delete(WAYS_TABLE_NAME, "$WAY_COL_END_MARKER_ID = $markerID", null)
        db.close()
         */
    }

    fun login() {
        // Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()


        val provider: OAuthProvider = DefaultOAuthProvider(
            "https://api06.dev.openstreetmap.org/oauth/request_token",
            "https://api06.dev.openstreetmap.org/oauth/access_token",
            "https://api06.dev.openstreetmap.org/oauth/authorize"
        )

        val consumer = OkHttpOAuthConsumer("")


        val connection = OsmConnection("https://api06.dev.openstreetmap.org/", null, null)

    }

}












