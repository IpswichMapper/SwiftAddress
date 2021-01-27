package com.mapitall.SwiftAddress

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.os.Bundle
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
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission

import androidx.core.content.ContextCompat
import layout.AddressNodes
import layout.StoreHouseNumbers

import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : AppCompatActivity(), MapEventsReceiver {

    private var DEBUG_TAG = "MainActivity"

    private lateinit var map : MapView
    private var markerList: MutableList<Marker> = mutableListOf()
    private var storeHouseNumbersObject : StoreHouseNumbers = StoreHouseNumbers(this)
    private var increment  = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set map details

        val sharedPreferences = getPreferences(MODE_PRIVATE)
        increment = sharedPreferences.getInt("increment", 2)
        setContentView(R.layout.activity_main)
        val ctx: Context = applicationContext
        Configuration.getInstance().load(
            ctx,
            PreferenceManager.getDefaultSharedPreferences(ctx)
        )

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)

        val eventsOverlay = MapEventsOverlay(this)
        map.overlays.add(0, eventsOverlay)

        val mapController = map.controller
        mapController.setZoom(3.0)

        // Zoom to current position when the app starts.
        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, false)
            val location = locationManager.getLastKnownLocation(provider!!)
            mapController.animateTo(GeoPoint(location!!.latitude, location.longitude))
            mapController.zoomTo(17, null)
            Log.i(DEBUG_TAG, "zoomed to location")
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

        // Onclicklistener and onlongpresslistener for the left arrow.
        // click open keypad, longpress automatically add housenumber

        val leftArrow = findViewById<ImageButton>(R.id.add_address_on_left)
        leftArrow.setOnClickListener {

            val coordinates = map.mapCenter
            val intent = Intent(this, Keypad::class.java)
            intent.putExtra("lat", coordinates.latitude)
            intent.putExtra("lon", coordinates.longitude)
            intent.putExtra("side", "left")
            val lastAddress = storeHouseNumbersObject.lastAddressEntry("left")
            intent.putExtra("last_address" , lastAddress)
            Log.i("increment before send", increment.toString())
            intent.putExtra("increment", increment)
            startActivityForResult(intent, 2)

        }
        leftArrow.setOnLongClickListener(View.OnLongClickListener {
            val addressToChange = storeHouseNumbersObject.lastAddressEntry("left")
            increment = sharedPreferences.getInt("increment", 2)

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

                storeHouseNumbersObject.addHouseNumber(addressToChange)
                addHousenumberMarker(addressToChange)
            }

            return@OnLongClickListener true
        })

        // Onclicklistener and onlongpresslistener for the right arrow.
        // click open keypad, longpress automatically add housenumber
        val rightArrow = findViewById<ImageButton>(R.id.add_address_on_right)
        rightArrow.setOnClickListener {

            val coordinates = map.mapCenter
            val intent = Intent(this, Keypad::class.java)
            intent.putExtra("lat", coordinates.latitude)
            intent.putExtra("lon", coordinates.longitude)
            intent.putExtra("side", "right")
            val lastAddress = storeHouseNumbersObject.lastAddressEntry("right")
            intent.putExtra("last_address" , lastAddress)
            intent.putExtra("increment", increment)
            startActivityForResult(intent, 3)
        }
        rightArrow.setOnLongClickListener(View.OnLongClickListener {
            val addressToChange = storeHouseNumbersObject.lastAddressEntry("right")
            increment = sharedPreferences.getInt("increment", 2)

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

                storeHouseNumbersObject.addHouseNumber(addressToChange)
                addHousenumberMarker(addressToChange)
            }

            return@OnLongClickListener true
        })

        val plusButton = findViewById<ImageButton>(R.id.zoom_in)
        val minusButton = findViewById<ImageButton>(R.id.zoom_out)
        val reCenterButton = findViewById<ImageButton>(R.id.recenter)
        val northButton = findViewById<ImageButton>(R.id.north_orientation)

        // Recenter the map to your current location if it exists. Zoom in if you are below
        // zoom level 17. Show a toast if the location isn't available.
        reCenterButton.setOnClickListener {
            try {
                val zoomInManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val criteria = Criteria()
                val provider = zoomInManager.getBestProvider(criteria, false)
                val location = zoomInManager.getLastKnownLocation(provider!!)

                if (map.zoomLevelDouble < 17) {
                    Log.i(DEBUG_TAG, "in the IF statement: ${map.zoomLevel}, ${map.zoomLevelDouble}")
                    map.controller.zoomTo(17.0)
                }
                map.controller.animateTo(GeoPoint(location!!.latitude, location.longitude))

                Log.i(DEBUG_TAG, "zoomLevel: ${map.zoomLevelDouble}")

                Log.i("test", "test")
            } catch (e : Exception) {
                e.printStackTrace()
                Toast.makeText(this,
                    getString(R.string.location_not_found),
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // zoom in and out buttons.
        plusButton.setOnClickListener {
            map.controller.zoomIn()
        }
        minusButton.setOnClickListener {
            map.controller.zoomOut()
        }

        // 4th parameter is the orientation to zoom to. "0f" means north.
        northButton.setOnClickListener {
            map.controller.animateTo(null, null, null, 0f)
        }

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

        map.setBuiltInZoomControls(false)

        // Displays all the housenumbers that have already been
        // created but haven't been stored to an OSM file yet.
        markerList = storeHouseNumbersObject.displayMarkers(map, markerList)

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
            Log.i(DEBUG_TAG, "Address Latitude: ${addressParcel!!.latitude}")
            if (addressParcel.housenumber != "") {
                storeHouseNumbersObject.addHouseNumber(addressParcel)
                addHousenumberMarker(addressParcel)
            }
            Log.i(DEBUG_TAG, "House number added to database.")
        }

        // Following if statement is for "keypad" activity if right button was pressed.
        else if (requestCode == 3 && resultCode == RESULT_OK) {

            val bundle = data?.extras
            val addressParcel = bundle?.getParcelable<AddressNodes>("address")
            Log.i(DEBUG_TAG, "Address Latitude: ${addressParcel!!.latitude}")
            if (addressParcel.housenumber != "") {
                storeHouseNumbersObject.addHouseNumber(addressParcel)
                addHousenumberMarker(addressParcel)
            }
            Log.i(DEBUG_TAG, "House number added to database.")
        }
    }

    // Function that switches imageries based on what was chosen in
    // "ChooseBackgroundImagery" activity
    private fun backgroundImagery(imagery: String) {

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
            storeHouseNumbersObject.undo()
        }
    }

    // add a housenumber marker to the map.
    private fun addHousenumberMarker(address: AddressNodes) {

        markerList.add(Marker(map))

        markerList.last().position = GeoPoint(address.latitude, address.longitude)
        markerList.last().icon = ContextCompat.getDrawable(this, R.drawable.address)
        markerList.last().setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        markerList.last().title = address.housenumber

        map.overlays.add(markerList.last())
        Log.i("map", "housenumber marker added")
    }

    // add a note to the map as a marker and to the database.
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
                storeHouseNumbersObject.addNote(noteContents, lat, lon)

                markerList.add(Marker(map))

                markerList.last().position = GeoPoint(lat, lon)
                markerList.last().setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
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
            storeHouseNumbersObject.writeToOsmFile()

            for (marker: Marker in markerList) {
                map.overlays.remove(marker)
                map.invalidate()
            }
        }
    }


}
