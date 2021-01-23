package com.mapitall.SwiftAddress

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Criteria
import android.location.LocationManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission

import androidx.core.content.ContextCompat
import layout.AddressNodes
import layout.StoreHousenumbers

import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : AppCompatActivity(), MapEventsReceiver {


    private lateinit var map : MapView
    private var markerList: MutableList<Marker> = mutableListOf()
    private var storeHousenumbersObject : StoreHousenumbers = StoreHousenumbers(this)
    private var increment = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set map details

        setContentView(R.layout.activity_main)
        val ctx: Context = applicationContext
        Configuration.getInstance().load(
            ctx,
            PreferenceManager.getDefaultSharedPreferences(ctx)
        )

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)


        val test = BoundingBox(0.00, 0.01, 0.02, 0.03)

        val eventsOverlay = MapEventsOverlay(this)
        map.overlays.add(0, eventsOverlay)

        val mapController = map.controller
        mapController.setZoom(3.0)
        var locationManager : LocationManager? = null
        try {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, false)
            val location = locationManager.getLastKnownLocation(provider!!)
            mapController.animateTo(GeoPoint(location!!.latitude, location.longitude))
            mapController.zoomTo(17, null)
            Log.i("test", "test")
        } catch (e : Exception) {
            e.printStackTrace()
        }

        // Ask for user permissions
        // TODO: improve this code so that it asks for external storage permission again
        //  if the user denied.
        if (checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED ||
                checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                    WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION), 1)
        }

        // Onclicklisteners on arrows to open up keypad
        val leftArrow = findViewById<ImageButton>(R.id.add_address_on_left)
        leftArrow.setOnClickListener {

            val coordinates = map.mapCenter
            val intent = Intent(this, Keypad::class.java)
            intent.putExtra("lat", coordinates.latitude)
            intent.putExtra("lon", coordinates.longitude)
            intent.putExtra("side", "left")
            val lastAddress = storeHousenumbersObject.lastAddressEntry("left")
            intent.putExtra("last_address" , lastAddress)

            startActivityForResult(intent, 2)

        }
        leftArrow.setOnLongClickListener(View.OnLongClickListener {
            val addressToChange = storeHousenumbersObject.lastAddressEntry("left")

            if (addressToChange != null) {
                Log.i("Long Click detected", "longclicklistener on arrow started")
                val vibrator : Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

                if(Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(
                            150, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(150)
                }
                try {
                    var numToIncrement = addressToChange.housenumber.toInt()
                    numToIncrement += increment
                    addressToChange.housenumber = numToIncrement.toString()
                } catch (e: Exception) {
                    var textToSet = "";
                    if (addressToChange.housenumber.isNotBlank()) {
                        for (c in addressToChange.housenumber) {
                            val intOrNot = c.toString().toIntOrNull()
                            if (intOrNot != null) {
                                textToSet += intOrNot.toString()
                                Log.i(textToSet, "text to set")
                            }
                        }
                        textToSet = (textToSet.toInt() + increment).toString()
                        addressToChange.housenumber = textToSet
                    }
                    Log.i("final text", textToSet)
                }
                addressToChange.latitude = map.mapCenter.latitude
                addressToChange.longitude = map.mapCenter.longitude
                addressToChange.buildingLevels = ""

                storeHousenumbersObject.addHousenumber(addressToChange)
                addHousenumberMarker(addressToChange)
            }

            return@OnLongClickListener true
        })

        val rightArrow = findViewById<ImageButton>(R.id.add_address_on_right)
        rightArrow.setOnClickListener {

            val coordinates = map.mapCenter
            val intent = Intent(this, Keypad::class.java)
            intent.putExtra("lat", coordinates.latitude)
            intent.putExtra("lon", coordinates.longitude)
            intent.putExtra("side", "right")
            val lastAddress = storeHousenumbersObject.lastAddressEntry("right")
            intent.putExtra("last_address" , lastAddress)

            startActivityForResult(intent, 3)
        }
        rightArrow.setOnLongClickListener(View.OnLongClickListener {
            val addressToChange = storeHousenumbersObject.lastAddressEntry("right")

            if (addressToChange != null) {
                Log.i("Long Click detected", "longclicklistener on arrow started")
                val vibrator : Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

                if(Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(
                            150, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(150)
                }
                try {
                    var numToIncrement = addressToChange.housenumber.toInt()
                    numToIncrement += increment
                    addressToChange.housenumber = numToIncrement.toString()
                } catch (e: Exception) {
                    var textToSet = "";
                    if (addressToChange.housenumber.isNotBlank()) {
                        for (c in addressToChange.housenumber) {
                            val intOrNot = c.toString().toIntOrNull()
                            if (intOrNot != null) {
                                textToSet += intOrNot.toString()
                                Log.i(textToSet, "text to set")
                            }
                        }
                        textToSet = (textToSet.toInt() + increment).toString()
                        addressToChange.housenumber = textToSet
                    }
                    Log.i("final text", textToSet)
                }
                addressToChange.latitude = map.mapCenter.latitude
                addressToChange.longitude = map.mapCenter.longitude
                addressToChange.buildingLevels = ""

                storeHousenumbersObject.addHousenumber(addressToChange)
                addHousenumberMarker(addressToChange)
            }

            return@OnLongClickListener true
        })



        // Onclicklistener to open activity to change background imagery.
        findViewById<ImageButton>(R.id.change_background_imagery_button).setOnClickListener {
            val intent = Intent(this, ChooseBackgroundImagery::class.java)
            startActivityForResult(intent, 1)
        }

        // Hide action bar (maybe this isn't a good idea since menu icon (three vertical bars)
        // works well with action bar.
        supportActionBar?.hide()

        // Shows current location
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()
        map.overlays.add(locationOverlay)

        // Allows you to pinch & zoom as well as rotate the map.
        val rotationGestureOverlay = RotationGestureOverlay(map)
        rotationGestureOverlay.isEnabled
        map.setMultiTouchControls(true)
        map.overlays.add(rotationGestureOverlay)

        // Displays all the housenumbers that have already been
        // created but haven't been stored to an OSM file yet.
        markerList = storeHousenumbersObject.displayMarkers(map, markerList)

        map.setBuiltInZoomControls(false)

        for(marker: Marker in markerList) {
            map.overlays.add(marker)
        }

    }

    override fun longPressHelper(p: GeoPoint?): Boolean {

        val location = map.mapCenter
        addNote(location.latitude, location.longitude)
        return true
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        return true
    }

    // Function to handle data received from other activities when the close.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Following if statement is for "choosing background imagery" activity
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val imageryChoice = data!!.getStringExtra("imagery_chosen")
            Log.i("imagery chosen option: " , imageryChoice.toString())

            backgroundImagery(imageryChoice.toString())
        }

        // TODO : Have different codes to remember increment number for the following
        //  two if statements.

        // Following if statement is for "keypad" activity if left arrow was pressed.
        else if (requestCode == 2 && resultCode == RESULT_OK) {

            val bundle = data?.extras
            val addressParcel = bundle?.getParcelable<AddressNodes>("address")
            Log.i("address latitude", addressParcel!!.latitude.toString())
            if (addressParcel.housenumber != "") {
                storeHousenumbersObject.addHousenumber(addressParcel)
                addHousenumberMarker(addressParcel)
            }
        }

        // Following if statement is for "keypad" activity if right button was pressed.
        else if (requestCode == 3 && resultCode == RESULT_OK) {

            val bundle = data?.extras
            val addressParcel = bundle?.getParcelable<AddressNodes>("address")
            Log.i("address latitude", addressParcel!!.latitude.toString())
            if (addressParcel.housenumber != "") {
                storeHousenumbersObject.addHousenumber(addressParcel)
                addHousenumberMarker(addressParcel)
            }

            Log.i("Database", "Housenumber added")

            Log.w("BUG LOCATION", "attempting to add marker to map")


        }
    }

    // Function that switches imageries based on what was chosen in
    // "ChooseBackgroundImagery" activity
    fun backgroundImagery(imagery: String) {

        // All the different tile sources
        val mapnik = TileSourceFactory.MAPNIK

        // Proof of concept that you can switch maps. These specific tiles
        // don't really have any advantages over mapnik for this application
        // This map also gives:
        // java.io.IOException: Cleartext HTTP traffic to openptmap.org not permitted
        // I'll keep it in here as proof of concept.
        val publicTransportMap = TileSourceFactory.PUBLIC_TRANSPORT

        // Mapbox imagery disabled because it doesn't work & there are licensing issues
        // This is the error that shows up:
        // java.io.IOException: Cleartext HTTP traffic to a.tiles.mapbox.com not permitted
        /*
        val mapbox_satellite = XYTileSource("mapbox",
                0,
                17,
                256,
                ".png",
            arrayOf(
                "http://a.tiles.mapbox.com/v3/openstreetmap.map-4wvf9l0l/",
                "http://b.tiles.mapbox.com/v3/openstreetmap.map-4wvf9l0l/",
                "http://c.tiles.mapbox.com/v3/openstreetmap.map-4wvf9l0l/")
        )
        */

        // Switch statement to set tile source based on what string was given.
        when(imagery) {
            "mapnik_imagery" -> map.setTileSource(mapnik)
            //"mapbox_satellite" -> map.setTileSource(mapbox_satellite)
            "public_transport_map" -> map.setTileSource(publicTransportMap)
        }
    }

    // If undo button is pressed
    fun undo(view: View) {
        if (!markerList.isEmpty()) {
            map.overlays.remove(markerList.last())
            map.invalidate()


            // Remove item from database
            storeHousenumbersObject.undo()
        }
    }

    private fun addHousenumberMarker(address: AddressNodes) {

        markerList.add(Marker(map))

        markerList.last().position = GeoPoint(address.latitude, address.longitude)
        markerList.last().icon = ContextCompat.getDrawable(this, R.drawable.address)
        markerList.last().setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        markerList.last().title = address.housenumber

        map.overlays.add(markerList.last())
        Log.i("map", "housenumber marker added")
    }

    private fun addNote(lat: Double, lon: Double) {
        Log.i("Long Click detected", "addNote() method started")
        val vibrator : Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        if(Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(
                    80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(80)
        }

        val addNoteBuilder = AlertDialog.Builder(this)
        addNoteBuilder.setTitle(getString(R.string.add_note))

        var noteContents : String
        val note = EditText(this)
        note.minLines = 5
        val container = FrameLayout(this)
        val params : FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        note.layoutParams = params
        note.gravity = Gravity.TOP

        container.addView(note)


        addNoteBuilder.setView(container)

        addNoteBuilder.setPositiveButton(getString(R.string.save_note)) { _, _ ->
            noteContents = note.text.toString()

            Log.i("inside positive button", "noteContents: $noteContents")
            if (noteContents != "") {
                storeHousenumbersObject.addNote(noteContents, lat, lon)

                markerList.add(Marker(map))

                markerList.last().position = GeoPoint(lat, lon)
                markerList.last().setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                markerList.last().title = noteContents
                markerList.last().icon = ContextCompat.getDrawable(this, R.drawable.note)
                map.overlays.add(markerList.last())

                Log.i("button press", "setPositiveButton pressed")
            }
        }

        addNoteBuilder.setNeutralButton(getString(R.string.cancel)) { _, _ -> }

        addNoteBuilder.create().show()

    }



    // Save all the data collected to an .osm file, clear markers
    fun saveFile(view : View) {

        if (checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.give_storage_permission),
                    Toast.LENGTH_SHORT).show()
        } else {
            storeHousenumbersObject.writeToOsmFile()

            for (marker: Marker in markerList) {
                map.overlays.remove(marker)
                map.invalidate()
            }
        }
    }


}
