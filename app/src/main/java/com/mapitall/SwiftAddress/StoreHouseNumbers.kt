package layout

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mapitall.SwiftAddress.R
import kotlinx.android.parcel.Parcelize
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.io.FileOutputStream as FileOutputStream

// AddressNodes Class which contains all
@Parcelize
data class AddressNodes(
        private val housenumber_ : String,
        private val street_ : String = "",
        private val lat : Double,
        private val lon : Double,
        private val side_ : String,
        private val buildingLevels_ : String) : Parcelable {

    var housenumber = housenumber_
    var street = street_
    var latitude = lat
    var longitude = lon
    var side = side_
    var buildingLevels = buildingLevels_

}

// Static class ("object") that helps storing housenumbers
// TODO : Make this work with a database
class StoreHouseNumbers(private val context: Context) : SQLiteOpenHelper(context,
        "address_database",
        null,
        1) {
    private val DEBUG_TAG = "StoreHouseNumbers"

    private val TABLE_NAME = "address_database"
    private val COL_ID = "ID"
    private val COL_HOUSENUMBER = "HOUSENUMBER"
    private val COL_STREET = "STREET"
    private val COL_LATITUDE = "LATITUDE"
    private val COL_LONGITUDE = "LONGITUDE"
    private val COL_NOTE = "NOTE"
    private val COL_SIDE = "SIDE"
    private val COL_BUILDINGLEVELS = "BUILDINGLEVELS"
    private val COL_TYPE = "TYPE"

    // Write housenumbers to OSM file
    fun writeToOsmFile() {

        val (addressTextToWrite, noteTextToWrite) = databaseToXml()

        var isExternalStorageReadOnly: Boolean = false
        val extStorageState = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            isExternalStorageReadOnly = true
        }
        Log.i("ExtStorageReadOnly", "$isExternalStorageReadOnly")
        var isExternalStorageAvailable: Boolean = false
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            isExternalStorageAvailable = true
        }

        Log.w("addressFile", addressTextToWrite)
        Log.w("noteFile", noteTextToWrite)
        if (isExternalStorageAvailable && !isExternalStorageReadOnly) {
            val current = SimpleDateFormat("dd-mm-yyyy-hh-mm-ss").format(Date())
            val addressFileName = "housenumbers$current.osm"
            val noteFileName = "notes$current.osc"

            Log.i("ExtStorageAvailable", "$isExternalStorageAvailable")
            val folderPath = Environment.getExternalStorageDirectory().absolutePath +
                    File.separator + "SwiftAddress" + File.separator

            if (!File(folderPath).exists()) {
                File(folderPath).mkdir()
            }


            val addressFile = File(folderPath + addressFileName)
            val addressFileOutputStream: FileOutputStream


            val noteFile = File(folderPath + noteFileName)
            val noteFileOutputStream : FileOutputStream
            try {

                if (addressTextToWrite != "") {
                    addressFile.createNewFile()
                    addressFileOutputStream = FileOutputStream(addressFile, false)
                    addressFileOutputStream.write(addressTextToWrite.toByteArray())
                    addressFileOutputStream.flush()
                    addressFileOutputStream.close()
                }
                if (noteTextToWrite != "") {
                    noteFile.createNewFile()
                    noteFileOutputStream = FileOutputStream(noteFile, false)
                    noteFileOutputStream.write(noteTextToWrite.toByteArray())
                    noteFileOutputStream.flush()
                    noteFileOutputStream.close()
                }
                if (addressTextToWrite == "" && noteTextToWrite == "") {
                    Toast.makeText(context,
                            context.getString(R.string.osm_files_empty),
                            Toast.LENGTH_SHORT)
                            .show()
                }


                Log.i("FileWrite", "check your files")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val db: SQLiteDatabase = this.writableDatabase
            db.execSQL("DELETE FROM $TABLE_NAME")

            Toast.makeText(context,
                    context.getString(R.string.saved_to_folder),
                    Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Data failed to save.", Toast.LENGTH_SHORT).show()
        }
    }

    // This converts a "AddressNodes" object to OSM-XML format.
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

                addressFileMiddle.append("<node id=\"$i\" lat=\"$latitude\" lon=\"$longitude\">\n") // opening tag
                addressFileMiddle.append("<tag k=\"addr:housenumber\" v=\"$housenumber\"/>\n") // housenumber
                if (street != "") {
                    addressFileMiddle.append("<tag k=\"addr:street\" v=\"$street\"/>\n") // street
                }
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

                Log.e(DEBUG_TAG, "data type wasn't note or address.")
            }
        }

        addressFileToWrite.append(addressFileMiddle)
        addressFileToWrite.append(addressFileEnd)

        noteFileToWrite.append(noteFileMiddle)
        noteFileToWrite.append(noteFileEnd)
        if (addressFileMiddle.toString() != "" && noteFileMiddle.toString() == "") {
            return Pair(addressFileToWrite.toString(), "")
        } else if (addressFileMiddle.toString() != "" && noteFileMiddle.toString() != "") {
            return Pair(addressFileToWrite.toString(), noteFileToWrite.toString())
        } else if (addressFileMiddle.toString() == "" && noteFileMiddle.toString() != "") {
            return Pair("", noteFileToWrite.toString())
        } else {
            return Pair("", "")
        }
    }

    // adds a note to an xml representing an OSC file.
    private fun addNoteToOscFile() {
        TODO("Not yet implemented")
    }

    // Creates database to store the Addresses and Notes
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE ${TABLE_NAME}(ID INTEGER PRIMARY KEY AUTOINCREMENT,
           |TYPE STRING,
           |HOUSENUMBER STRING,
           |STREET STRING,
           |LATITUDE REAL NOT NULL,
           |LONGITUDE REAL NOT NULL,
           |SIDE STRING,
           |BUILDINGLEVELS STRING,
           |NOTE STRING);""".trimMargin())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${TABLE_NAME}")
        onCreate(db)
    }

    // Adds housenumber to database once it is created.
    fun addHouseNumber(address: AddressNodes) {

        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COL_HOUSENUMBER, address.housenumber)
        contentValues.put(COL_STREET, address.street)
        contentValues.put(COL_LATITUDE, address.latitude)
        contentValues.put(COL_LONGITUDE, address.longitude)
        contentValues.put(COL_SIDE, address.side)
        contentValues.put(COL_TYPE, "Address")
        contentValues.put(COL_BUILDINGLEVELS, address.buildingLevels)

        val result: Long = db.insert(TABLE_NAME, null, contentValues)

        if (result == -1L) {
            Log.e("Inserting database item", "failed at db.insert")
        } else {
            Log.i("Inserting database item", "Successful")
        }
    }

    // Adds notes to database once it is created.
    fun addNote(noteContents: String, lat: Double, lon: Double) {

        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COL_NOTE, noteContents)
        contentValues.put(COL_LATITUDE, lat)
        contentValues.put(COL_LONGITUDE, lon)
        contentValues.put(COL_TYPE, "Note")

        val result: Long = db.insert(TABLE_NAME, null, contentValues)

        if (result == -1L) {
            Log.e("Inserting database item", "failed to insert data")
        } else {
            Log.i("Inserting database item", "Successful")
        }
    }

    // TODO: Make this work with database
    fun undo() {
        val db: SQLiteDatabase = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME WHERE ID = (SELECT MAX(ID) FROM $TABLE_NAME);")
    }

    // Displays Markers from Database on the map when app is launched.
    fun displayMarkers(map: MapView, marker_list: MutableList<Marker>)
            : MutableList<Marker> {
        val db: SQLiteDatabase = this.readableDatabase
        var str = StringBuilder()

        Log.i("Database", "displayMarkers function started")

        var c: Cursor = db.query(TABLE_NAME, null, null, null,
                null, null, "ID ASC")

        while (c.moveToNext()) {
            if (c.getString(c.getColumnIndex(COL_TYPE)) == "Address") {

                var housenumber = c.getString(c.getColumnIndex(COL_HOUSENUMBER))
                var street = c.getString(c.getColumnIndex(COL_STREET))
                var latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                var longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))

                marker_list.add(Marker(map))
                marker_list.last().position = GeoPoint(latitude, longitude)
                marker_list.last()
                        .setIcon(ContextCompat.getDrawable(context, R.drawable.address))
                marker_list.last().setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker_list.last().title = housenumber
                Log.i("Marked added", "Address")

            } else if (c.getString(c.getColumnIndex(COL_TYPE)) == "Note") {
                var noteContents = c.getString(c.getColumnIndex(COL_NOTE))
                var latitude = c.getDouble(c.getColumnIndex(COL_LATITUDE))
                var longitude = c.getDouble(c.getColumnIndex(COL_LONGITUDE))

                marker_list.add(Marker(map))
                marker_list.last().position = GeoPoint(latitude, longitude)
                marker_list.last().icon = ContextCompat.getDrawable(context, R.drawable.address)
                marker_list.last().setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker_list.last().title = noteContents
                Log.i("Marker added", "Note")

            } else {
                Log.e(
                        "Adding a marker failed",
                        "data type wasn't \"Note\" or \"Address\""
                )

            }
        }
        return marker_list
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
}








