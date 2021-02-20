package layout

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mapitall.SwiftAddress.MainActivity
import com.mapitall.SwiftAddress.Map
import com.mapitall.SwiftAddress.MarkerWindow
import com.mapitall.SwiftAddress.R
import kotlinx.parcelize.Parcelize
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat.getDateTimeInstance
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

// AddressNodes Class which conveniently packages all the information about
// an address object into one data class that can be shared accross activities.
@Parcelize
data class AddressNodes(
    var housenumber : String,
    val street : String = "",
    var latitude : Double,
    var longitude : Double,
    val side : String,
    var buildingLevels : String) : Parcelable

class StoreHouseNumbers(private val context: Context) : SQLiteOpenHelper(context,
        "address_database",
        null,
        1) {
    private val TAG = "StoreHouseNumbers"

    // All the database table columns.
    private val TABLE_NAME = "address_database"
    private val TEMP_TABLE_NAME = "saved_address_database"
    private val COL_ID = "ID"
    private val COL_HOUSENUMBER = "HOUSENUMBER"
    private val COL_STREET = "STREET"
    private val COL_LATITUDE = "LATITUDE"
    private val COL_LONGITUDE = "LONGITUDE"
    private val COL_NOTE = "NOTE"
    private val COL_SIDE = "SIDE"
    private val COL_BUILDINGLEVELS = "BUILDINGLEVELS"
    private val COL_TYPE = "TYPE"

    // Write housenumbers to .osm file and notes to .osc file.
    fun writeToOsmFile() {

        val (addressTextToWrite, noteTextToWrite) = databaseToXml()

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
            val addressFileName = "housenumbers.osm"
            val noteFileName = "notes.osc"

            Log.i("ExtStorageAvailable", "$isExternalStorageAvailable")


            val folderPath = context.getExternalFilesDir(null)!!
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
                if (addressTextToWrite == "" && noteTextToWrite == "") {
                    Toast.makeText(context,
                            context.getString(R.string.osm_files_empty),
                            Toast.LENGTH_SHORT)
                            .show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    // This converts a "AddressNodes" object to OSM-XML format (.osm)
    // Converts a note to OsmChange format (.osc)
    private fun databaseToXml(): Pair<String, String> {

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

        val c: Cursor = db.query(TABLE_NAME, null, null, null,
                null, null, "ID ASC")


        var i = -1
        var j = -1
        while (c.moveToNext()) {
            val type = c.getString(c.getColumnIndex(COL_TYPE))
            if (type == "Address") {
                val housenumber = c.getString(c.getColumnIndex(COL_HOUSENUMBER))
                val street = c.getString(c.getColumnIndex(COL_STREET))
                val latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))
                val buildingLevels = c.getString(c.getColumnIndex(COL_BUILDINGLEVELS))

                addressFileMiddle.append("<node id=\"$i\" lat=\"$latitude\" lon=\"$longitude\">\n") // opening tag
                addressFileMiddle.append("<tag k=\"addr:housenumber\" v=\"$housenumber\"/>\n") // housenumber
                if (street != "") {
                    addressFileMiddle.append("<tag k=\"addr:street\" v=\"$street\"/>\n") // street
                }
                if (buildingLevels != "") {
                    val parts = buildingLevels.split(" ") //

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
                i--
            } else if (type == "Note") {
                val contents = c.getString(c.getColumnIndex(COL_NOTE))
                val latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))

                noteFileMiddle.append("<note id=\"$j\" lat=\"$latitude\" lon=\"$longitude\">\n")
                noteFileMiddle.append("<comment text=\"$contents\" />\n")
                noteFileMiddle.append("</note>\n")
                j--
            } else {

                Log.i(TAG, "data type wasn't note or address. It was mostly likely an image")
            }
        }
        c.close()

        addressFileToWrite.append(addressFileMiddle)
        addressFileToWrite.append(addressFileEnd)

        noteFileToWrite.append(noteFileMiddle)
        noteFileToWrite.append(noteFileEnd)
        if (addressFileMiddle.toString() != "" && noteFileMiddle.toString() == "") {
            Log.i(TAG, "Writing Addresses to File")
            return Pair(addressFileToWrite.toString(), "")
        } else if (addressFileMiddle.toString() != "" && noteFileMiddle.toString() != "") {
            Log.i(TAG, "Writing Addresses and Notes to File")
            return Pair(addressFileToWrite.toString(), noteFileToWrite.toString())
        } else if (addressFileMiddle.toString() == "" && noteFileMiddle.toString() != "") {
            Log.i(TAG, "Writing Notes to File")
            return Pair("", noteFileToWrite.toString())
        } else {
            Log.i(TAG, "Writing Nothing to File")
            return Pair("", "")
        }
    }

    // Creates database to store the Addresses and Notes
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE $TABLE_NAME(ID INTEGER PRIMARY KEY AUTOINCREMENT,
           |TYPE STRING,
           |HOUSENUMBER STRING,
           |STREET STRING,
           |LATITUDE REAL NOT NULL,
           |LONGITUDE REAL NOT NULL,
           |SIDE STRING,
           |BUILDINGLEVELS STRING,
           |NOTE STRING);""".trimMargin())

        db.execSQL("CREATE TABLE $TEMP_TABLE_NAME AS SELECT * FROM $TABLE_NAME")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${TABLE_NAME}")
        onCreate(db)
    }

    // Remove all rows from the main table, and move them to a "temporary table" (so that they
    // can be recovered later if need be)
    fun clearDatabase() {
        val db: SQLiteDatabase = this.writableDatabase
        var doNotExecuteNext = false
        try {
            db.delete(TEMP_TABLE_NAME, null, null)
        } catch (e : SQLiteException) {
            e.printStackTrace()
            db.execSQL("CREATE TABLE $TEMP_TABLE_NAME AS SELECT * FROM $TABLE_NAME")
            doNotExecuteNext = true
        }
        if (!doNotExecuteNext) {
            db.execSQL("INSERT INTO $TEMP_TABLE_NAME SELECT * FROM $TABLE_NAME")
        }
        db.delete(TABLE_NAME, null, null)
        Log.i(TAG, "Database cleared.")
    }

    /*
    fun clearDatabaseToTrash() {
        val db: SQLiteDatabase = this.writableDatabase
    }
    */

    // Adds housenumber to database once it is created.
    fun addHouseNumber(address: AddressNodes) : Int {

        val db: SQLiteDatabase = this.writableDatabase
        val dbRead : SQLiteDatabase = this.readableDatabase
        val contentValues = ContentValues()

        contentValues.put(COL_HOUSENUMBER, address.housenumber)
        contentValues.put(COL_STREET, address.street)
        contentValues.put(COL_LATITUDE, address.latitude)
        contentValues.put(COL_LONGITUDE, address.longitude)
        contentValues.put(COL_SIDE, address.side)
        contentValues.put(COL_TYPE, "Address")
        contentValues.put(COL_BUILDINGLEVELS, address.buildingLevels)

        val result: Long = db.insert(TABLE_NAME, null, contentValues)

        return result.toInt()

    }

    // Adds notes to database once it is created.
    fun addNote(noteContents: String, lat: Double, lon: Double) : Int {

        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COL_NOTE, noteContents)
        contentValues.put(COL_LATITUDE, lat)
        contentValues.put(COL_LONGITUDE, lon)
        contentValues.put(COL_TYPE, "Note")

        val result: Long = db.insert(TABLE_NAME, null, contentValues)
        return result.toInt()
    }

    // Return the item type of the last item that was added.
    fun lastItemType() : String {
        val dbRead : SQLiteDatabase = this.readableDatabase
        val db: SQLiteDatabase = this.writableDatabase
        val c : Cursor = dbRead.query(TABLE_NAME, null, null, null,
                null, null, "ID DESC")
        c.moveToNext()
        Log.i(TAG, "Item Type: ${c.getString(1)}")
        return c.getString(1)
    }

    // removes the last object from database.
    fun undo(isAnImage : Boolean){
        val db: SQLiteDatabase = this.writableDatabase

        if (isAnImage) {
            val dbRead : SQLiteDatabase = this.readableDatabase
            val c: Cursor = dbRead.query(TABLE_NAME, null, null, null,
                    null, null, "ID DESC")
            c.moveToNext()
            val absolutePath = c.getString(c.getColumnIndex(COL_NOTE))
            File(absolutePath).delete()
            Log.i(TAG, "Item Type: ${c.getString(1)}")
            c.close()
        }

        db.execSQL("DELETE FROM $TABLE_NAME WHERE ID = (SELECT MAX(ID) FROM $TABLE_NAME);")



    }


    // Displays Markers from Database on the map when app is launched.

    fun displayMarkers(mapClass: Map, mainActivity: MainActivity) {
        val db: SQLiteDatabase = this.readableDatabase
        val markerHashMap = HashMap<Int, Marker>()

        Log.i(TAG, "displayMarkers function started")

        val c: Cursor = db.query(TABLE_NAME, null, null, null,
                null, null, "ID ASC")

        while (c.moveToNext()) {
            if (c.getString(c.getColumnIndex(COL_TYPE)) == "Address") {

                val housenumber = c.getString(c.getColumnIndex(COL_HOUSENUMBER))
                var street = c.getString(c.getColumnIndex(COL_STREET))
                // TODO : Show street in popup
                val latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))
                val id = c.getInt(c.getColumnIndex(COL_ID))
                /*
                markerList.add(Marker(map))
                markerList.last().position = GeoPoint(latitude, longitude)
                markerList.last().icon = ContextCompat.getDrawable(context, R.drawable.address)
                markerList.last().setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                markerList.last().title = housenumber
                */

                markerHashMap[id] = Marker(mapClass.mapView)
                markerHashMap.getValue(id).position = GeoPoint(latitude, longitude)
                markerHashMap.getValue(id).icon = ContextCompat.getDrawable(context,
                        R.drawable.address)
                markerHashMap.getValue(id).setAnchor(
                        Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

                val infoWindow = MarkerWindow(
                        R.layout.address_press_layout_linear,
                        mapClass,
                        context,
                        id,
                        mainActivity)
                // markerList.last().infoWindow = infoWindow
                markerHashMap.getValue(id).infoWindow = infoWindow
                Log.i(TAG, "Address Marker added")

            } else if (c.getString(c.getColumnIndex(COL_TYPE)) == "Note") {
                val noteContents = c.getString(c.getColumnIndex(COL_NOTE))
                val latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))
                val id = c.getInt(c.getColumnIndex(COL_ID))
                /*
                markerList.add(Marker(map))
                markerList.last().position = GeoPoint(latitude, longitude)
                markerList.last().icon = ContextCompat.getDrawable(context, R.drawable.note)
                markerList.last().setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                markerList.last().title = noteContents
                Log.i(DEBUG_TAG, "Note Marker added")
                */

                markerHashMap[id] = Marker(mapClass.mapView)
                markerHashMap.getValue(id).position = GeoPoint(latitude, longitude)
                markerHashMap.getValue(id).icon = ContextCompat.getDrawable(context,
                        R.drawable.note)
                markerHashMap.getValue(id).setAnchor(
                        Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                markerHashMap.getValue(id).title = noteContents
                Log.i(TAG, "Note Marker added")

            } else if (c.getString(c.getColumnIndex(COL_TYPE)) == "Image") {
                val latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))
                val id = c.getInt(c.getColumnIndex(COL_ID))
                val imageName = c.getString(c.getColumnIndex(COL_NOTE))
                /*
                markerList.add(Marker(map))
                markerList.last().position = GeoPoint(latitude, longitude)
                markerList.last().icon = ContextCompat.getDrawable(context, R.drawable.camera)
                markerList.last().setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                markerList.last().title = c.getString(c.getColumnIndex(COL_NOTE)) // TODO : Show actual image
                Log.i(DEBUG_TAG, "Image Marker added")
                */

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
        mapClass.mapView.invalidate()
        mapClass.setMarkerHashMap(markerHashMap)
    }

    // Gets last address that was entered
    fun lastAddressEntry(side : String) : AddressNodes?{
        val db: SQLiteDatabase = this.readableDatabase
        var lastAddress: AddressNodes? = null
        var c: Cursor = db.query(TABLE_NAME, null, null, null,
                null, null, "ID DESC")



        var runCondition = true
        while (c.moveToNext() && runCondition) {
            if (c.getString(c.getColumnIndex(COL_TYPE)) == "Address" &&
                    c.getString(c.getColumnIndex(COL_SIDE)) == side) {
                lastAddress = AddressNodes(
                        c.getString(c.getColumnIndex(COL_HOUSENUMBER)),
                        c.getString(c.getColumnIndex(COL_STREET)),
                        c.getDouble(c.getColumnIndex(COL_LATITUDE)),
                        c.getDouble(c.getColumnIndex(COL_LONGITUDE)),
                        c.getString(c.getColumnIndex(COL_SIDE)),
                        c.getString(c.getColumnIndex(COL_BUILDINGLEVELS))
                )
                runCondition = false
            }
        }

        c.close()
        return lastAddress

    }

    // Add an row containing information about an image stored in internal storage
    @SuppressLint("Recycle")
    fun addImage(absolutePath : String, lat : Double, lon : Double) : Int {
        val db : SQLiteDatabase = this.writableDatabase
        val dbRead : SQLiteDatabase = this.readableDatabase
        val contentValues = ContentValues()

        contentValues.put(COL_NOTE, absolutePath)
        contentValues.put(COL_TYPE, "Image")
        contentValues.put(COL_LATITUDE, lat)
        contentValues.put(COL_LONGITUDE, lon)

        val result: Long = db.insert(TABLE_NAME, null, contentValues)
        return result.toInt()

    }

    // Remove marker at specific position.
    fun removeAt(housenumberID: Int) {

        val dbRead : SQLiteDatabase = this.readableDatabase
        val c : Cursor = dbRead.query(TABLE_NAME, null, null, null,
                null, null, "ID DESC")
        c.moveToNext()
        val itemType = c.getString(1)
        Log.i(TAG, "Item Type: $itemType")

        // TODO : Check this works when you add image deletion functionality.
        if (itemType == "Image") {
            val absolutePath = c.getString(c.getColumnIndex(COL_NOTE))
            File(absolutePath).delete()
            c.close()
        }

        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME WHERE ID = $housenumberID;")
        db.close()
    }

    // Change location of marker after it has been moved.
    fun changeLocation(ID : Int, lat : Double, lon : Double) {
        val db = this.writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET LATITUDE = $lat WHERE ID = $ID")
        db.execSQL("UPDATE $TABLE_NAME SET LONGITUDE = $lon WHERE ID = $ID")
        db.close()
    }

    // Gets old data from a "temporary table", and overwrites current table with
    // rows from "temporary table". The "temporary table" consists of data from
    // the last time the user saved.
    fun recoverData(mapClass : Map, mainActivity: MainActivity) {

        Log.i(TAG, "Attempting to recover data.")
        val db = this.writableDatabase

        val recoverDataDialog = AlertDialog.Builder(context)
        recoverDataDialog.setPositiveButton(context.getString(R.string.recover)) { _, _ ->
            try {
                db.delete(TABLE_NAME, null, null)
                db.execSQL("INSERT INTO $TABLE_NAME SELECT * FROM $TEMP_TABLE_NAME")
                val rows = db.delete(TABLE_NAME, "TYPE = 'Image'", null)
                Log.i(TAG, "$rows rows deleted (image rows)")
                displayMarkers(mapClass, mainActivity)
            } catch (e : SQLiteException) {
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
}








