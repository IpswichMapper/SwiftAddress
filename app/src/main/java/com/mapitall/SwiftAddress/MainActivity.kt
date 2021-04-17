package com.mapitall.SwiftAddress

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.Gravity.CENTER
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.ConfigurationCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.exifinterface.media.ExifInterface
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.mancj.slideup.SlideUp
import com.mancj.slideup.SlideUpBuilder
import com.mapitall.SwiftAddress.BackgroundImagery.BackgroundImagery
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(),
        MapEventsReceiver,
        GestureDetector.OnGestureListener,
        PopupMenu.OnMenuItemClickListener,
        LocationListener {

    private var currentImagePath: String? = null
    private var currentAudioPath: String? = null
    private var TAG = "MainActivity"

    private lateinit var map : Map
    private var markerList: MutableList<Marker> = mutableListOf()
    private lateinit var imagery : String
    private lateinit var locationOverlay : MyLocationNewOverlay
    private var storeHouseNumbersObject: StoreHouseNumbers = StoreHouseNumbers(this)
    private var increment by Delegates.notNull<Int>()
    private var houseName = ""
    private var noOnTouchActions = true
    private var flingUpDetected = false
    private var flingLeftDetected = false
    private var flingRightDetected = false
    private var longPressDetected = false
    private lateinit var slideUp: SlideUp
    var creatingInterpolationWay = false

    var polyline : Polyline? = null
    var geoPoints : ArrayList<GeoPoint>? = null
    var startMarkerID : Long? = null

    @SuppressLint("ClickableViewAccessibility")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // set map details
        map = Map(findViewById(R.id.map), this, this)

        // get preferences
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (sp.getBoolean("screen_timeout", false)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        increment = sp.getInt("increment", 2)
        backgroundImagery()

        val items = ArrayList<OverlayItem>()
        items.add(OverlayItem("Title", "Description", GeoPoint(0.0, 0.0)))

        // zoom to current location when app starts.
        val mapController = map.mapView.controller
        mapController.setZoom(3.0)

        val eventsOverlay = MapEventsOverlay(this)
        map.mapView.overlays.add(0, eventsOverlay)

        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, false)
            val location = locationManager.getLastKnownLocation(provider!!)
            mapController.animateTo(GeoPoint(location!!.latitude, location.longitude))
            mapController.zoomTo(17, null)
            Log.i(TAG, "zoomed to location")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val gestureDetector = GestureDetectorCompat(this, this)

        if (checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED ||
                checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(
                    WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION
            ), 1
            )
        }

        // Onclicklistener and onlongpresslistener for the left arrow.
        // click open keypad, longpress automatically add housenumber

        val leftArrow = findViewById<ImageButton>(R.id.add_address_on_left)


        val swipeUpRelativeLayout = findViewById<View>(R.id.swipe_up_relative_layout)
        slideUp = SlideUpBuilder(
                swipeUpRelativeLayout
        ).withStartState(SlideUp.State.HIDDEN)
                .withGesturesEnabled(false)
                .withStartGravity(Gravity.BOTTOM)
                .build()


        leftArrow.setOnTouchListener { _, event ->
            Log.i(TAG, "leftArrow: onTouchListener Called")

            flingUpDetected = false

            gestureDetector.onTouchEvent(event)
            // IF YOU SWIPE UP ON THE LEFT ARROW
            if (flingUpDetected) showSwipeUpKeypad(true)
            noOnTouchActions = true

            return@setOnTouchListener super.onTouchEvent(event)
        }


        leftArrow.setOnLongClickListener {

            val addressToChange = storeHouseNumbersObject.lastAddressEntry("left")
            increment = sp.getInt("increment", 2)

            if (addressToChange != null) {
                Log.i(TAG, "longPressDetected & address is not null")

                vibrate(150, VibrationEffect.DEFAULT_AMPLITUDE)

                addressToChange.housenumber = incrementAddress(addressToChange.housenumber)
                addressToChange.latitude = map.mapView.mapCenter.latitude
                addressToChange.longitude = map.mapView.mapCenter.longitude
                addressToChange.buildingLevels = ""

                val id = storeHouseNumbersObject.addHouseNumber(addressToChange)
                if (id != -1L) {
                    map.addHousenumberMarker(addressToChange, id)
                } else {
                    Toast.makeText(this, R.string.failed_save, Toast.LENGTH_SHORT).show()
                }

                map.mapView.invalidate()
            } else {
                Toast.makeText(this, getString(R.string.add_address_first),
                        Toast.LENGTH_SHORT).show()
            }
            return@setOnLongClickListener true
        }

        leftArrow.setOnClickListener {
            Log.i(TAG, "onclickcalled")
            if (noOnTouchActions) {
                val coordinates = map.mapView.mapCenter
                val intent = Intent(this, Keypad::class.java)
                intent.putExtra("lat", coordinates.latitude)
                intent.putExtra("lon", coordinates.longitude)
                intent.putExtra("side", "left")
                val lastAddress = storeHouseNumbersObject.lastAddressEntry("left")
                intent.putExtra("last_address", lastAddress)
                Log.i("increment before send", increment.toString())
                intent.putExtra("increment", increment)
                startActivityForResult(intent, 2)

            }
        }

        // Onclicklistener and onlongpresslistener for the right arrow.
        // click open keypad, longpress automatically add housenumber
        val rightArrow = findViewById<ImageButton>(R.id.add_address_on_right)

        rightArrow.setOnTouchListener { _, event ->

            flingUpDetected = false

            noOnTouchActions = false

            gestureDetector.onTouchEvent(event)

            // IF YOU SWIPE UP ON THE RIGHT ARROW
            if (flingUpDetected) showSwipeUpKeypad(false)
            else noOnTouchActions = true

            return@setOnTouchListener super.onTouchEvent(event)
        }

        val degreeLatitude = Location.convert(map.mapView.mapCenter.latitude, Location.FORMAT_DEGREES)
        Log.i(TAG, "degreeLatitude  $degreeLatitude")

        rightArrow.setOnLongClickListener {
            val addressToChange = storeHouseNumbersObject.lastAddressEntry("right")
            increment = sp.getInt("increment", 2)
            Log.i(TAG, "increment $increment")
            if (addressToChange != null) {
                Log.i(TAG, "longPressDetected & address is not null")

                vibrate(150, VibrationEffect.DEFAULT_AMPLITUDE)

                addressToChange.housenumber = incrementAddress(addressToChange.housenumber)
                addressToChange.latitude = map.mapView.mapCenter.latitude
                addressToChange.longitude = map.mapView.mapCenter.longitude
                addressToChange.buildingLevels = ""

                val id = storeHouseNumbersObject.addHouseNumber(addressToChange)
                if (id != -1L) {
                    map.addHousenumberMarker(addressToChange, id)
                } else {
                    Toast.makeText(this, R.string.failed_save, Toast.LENGTH_SHORT).show()
                }

                map.mapView.invalidate()
            } else {
                Toast.makeText(this, getString(R.string.add_address_first),
                        Toast.LENGTH_SHORT).show()
            }
            return@setOnLongClickListener true

        }

        rightArrow.setOnClickListener {

            if (noOnTouchActions) {
                val coordinates = map.mapView.mapCenter
                val intent = Intent(this, Keypad::class.java)
                intent.putExtra("lat", coordinates.latitude)
                intent.putExtra("lon", coordinates.longitude)
                intent.putExtra("side", "right")
                val lastAddress = storeHouseNumbersObject.lastAddressEntry("right")
                intent.putExtra("last_address", lastAddress)
                intent.putExtra("increment", increment)
                startActivityForResult(intent, 3)
            }
        }


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

                Log.i(TAG, location!!.latitude.toString())
                if (map.mapView.zoomLevelDouble < 17) {
                    map.mapView.controller.zoomTo(17.0)
                }
                map.mapView.controller.animateTo(GeoPoint(location.latitude, location.longitude))

                Log.i(TAG, "zoomLevel: ${map.mapView.zoomLevelDouble}")
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                        this,
                        getString(R.string.location_not_found),
                        Toast.LENGTH_SHORT
                )
                        .show()
            }
        }


        swipeUpRelativeLayout.setOnTouchListener { _, event ->
            flingLeftDetected = false
            flingRightDetected = false

            val miniKeypadSide = findViewById<TextView>(R.id.mini_keypad_side)

            Log.i(TAG, "swipeUpRelativeLayout OnTouchListener called")
            gestureDetector.onTouchEvent(event)

            Log.i(TAG, "flingLeftDetected: $flingLeftDetected")
            Log.i(TAG, "flingRightDetected, $flingRightDetected")
            Log.i(TAG, "text: ${miniKeypadSide.text}")

            if (flingLeftDetected && findViewById<TextView>(R.id.mini_keypad_side).text ==
                    getString(R.string.left_side)) {

                Log.i(TAG, "flingLeftDetected")
                switchSwipeUpKeypadSide(false)
            }
            else if (flingRightDetected && findViewById<TextView>(R.id.mini_keypad_side).text ==
                    getString(R.string.right_side)) {

                Log.i(TAG, "flingRightDetected")
                switchSwipeUpKeypadSide(true)
            } else {
                Log.i(TAG, "onTouch didn't satify conditions")
            }

            return@setOnTouchListener true
        }

        // zoom in and out buttons.
        plusButton.setOnClickListener {
            map.mapView.controller.zoomIn()
        }
        minusButton.setOnClickListener {
            map.mapView.controller.zoomOut()
        }

        // 4th parameter is the orientation to zoom to. "0f" means north.
        northButton.setOnClickListener {
            map.mapView.controller.animateTo(null, null, null, 0f)
        }

        // Onclicklistener to open activity to change background imagery.
        findViewById<ImageButton>(R.id.change_background_imagery_button).setOnClickListener {
            val backgroundImageryIntent = Intent(this, BackgroundImagery::class.java)
            startActivityForResult(backgroundImageryIntent, 1)
        }

        val moveMarkerButton = findViewById<Button>(R.id.bubble_title)

        // Hide action bar (maybe this isn't a good idea since menu icon (three vertical bars)
        // works well with action bar.
        supportActionBar?.hide()

        // Shows current location
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map.mapView)
        locationOverlay.enableMyLocation()
        map.mapView.overlays.add(locationOverlay)

        // Allows you to pinch & zoom as well as rotate the map.
        val rotationGestureOverlay = RotationGestureOverlay(map.mapView)
        rotationGestureOverlay.isEnabled
        map.mapView.setMultiTouchControls(true)
         map.mapView.overlays.add(rotationGestureOverlay)
        map.mapView.tileRequestCompleteHandler
        // Turns off automatic zoom buttons
        map.mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        val navMenu = findViewById<NavigationView>(R.id.nav_menu)

        navMenu.setNavigationItemSelectedListener { menuItem ->
            val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
            val itemID = menuItem.itemId
            when(itemID) {
                R.id.recover_data_menu_option -> {

                    drawerLayout.closeDrawer(GravityCompat.START)
                    storeHouseNumbersObject.recoverData(map, this)
                }
                R.id.save_data_menu_option -> {

                    drawerLayout.closeDrawer(GravityCompat.START)
                    saveData(findViewById(itemID))
                }
                R.id.download_addresses_menu_option -> {
                    val intent = Intent(this, BackgroundImagery::class.java)
                    intent.putExtra("specific_fragment", "download-addresses")
                    startActivityForResult(intent, 8)
                }
                R.id.upload_data_menu_option -> uploadData()
                R.id.settings_menu_option -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, Settings::class.java)
                    startActivityForResult(intent, 7)
                }
            }

            return@setNavigationItemSelectedListener true
        }

    }

    private fun vibrate(milliSeconds: Int, vibrationEffect: Int) {

        val vibrator: Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                    VibrationEffect.createOneShot(
                            milliSeconds.toLong(), vibrationEffect
                    )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(milliSeconds.toLong())
        }
    }

    private fun incrementAddress(houseNum : String,
                                 numIncrement : Int = this.increment) : String {
        var houseNumber = houseNum
        try {
            var numToIncrement = houseNumber.toInt()
            numToIncrement += numIncrement
            houseNumber = numToIncrement.toString()
        } catch (e: Exception) {
            var textToSet = ""
            if (houseNumber.isNotBlank()) {
                for (c in houseNumber) {
                    val intOrNot = c.toString().toIntOrNull()
                    if (intOrNot != null) {
                        textToSet += intOrNot.toString()
                        Log.i(textToSet, "text to set")
                    }
                }
                textToSet = (textToSet.toInt() + numIncrement).toString()
                houseNumber = textToSet
            }
            Log.i(TAG, "Final Text: $textToSet")
        }

        return houseNumber
    }

    private fun uploadData() {
        Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()
    }

    override fun onLocationChanged(location: Location) {
        map.mapView.overlays.remove(locationOverlay)
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map.mapView)
        locationOverlay.enableMyLocation()
        map.mapView.overlays.add(locationOverlay)
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        val location = map.mapView.mapCenter
        addNote(location.latitude, location.longitude)
        return true
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        InfoWindow.closeAllInfoWindowsOn(map.mapView)
        return true
    }

    // Function to handle data received from other activities when the close.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Following if statement is for "BackgroundImagery" activity
        if (requestCode == 1 && resultCode == RESULT_OK) {
            backgroundImagery()
            // Redraw markers in case house numbers were downloaded.
            restart()
        }

        // Following if statement is for "keypad" activity if left arrow was pressed.
        else if (requestCode == 2 && resultCode == RESULT_OK) {

            val bundle = data?.extras
            val addressParcel = bundle?.getParcelable<AddressNodes>("address")
            Log.i(TAG, "Address Latitude: ${addressParcel!!.latitude}")
            if (addressParcel.housenumber != "") {
                val id = storeHouseNumbersObject.addHouseNumber(addressParcel)
                if (id != -1L) {
                    map.addHousenumberMarker(addressParcel, id)
                } else {
                    Toast.makeText(this, R.string.failed_save, Toast.LENGTH_SHORT).show()
                }
            }
            Log.i(TAG, "House number added to database.")
        }

        // Following if statement is for "keypad" activity if right button was pressed.
        else if (requestCode == 3 && resultCode == RESULT_OK) {

            val bundle = data?.extras
            val addressParcel = bundle?.getParcelable<AddressNodes>("address")
            Log.i(TAG, "Address Latitude: ${addressParcel!!.latitude}")
            if (addressParcel.housenumber != "") {
                val id = storeHouseNumbersObject.addHouseNumber(addressParcel)
                if (id != -1L) {
                    map.addHousenumberMarker(addressParcel, id)
                } else {
                    Toast.makeText(this, R.string.failed_save, Toast.LENGTH_SHORT).show()
                }
            }
            Log.i(TAG, "House number added to database.")
        }

        /*
        else if (requestCode == 4 && resultCode == RESULT_OK) {
            val bundle = data?.extras
            val filePath: String
            if (bundle != null) {

                val current = SimpleDateFormat("dd-mm-yyyy-hh-mm-ss").format(Date())
                val fileName = "image-$current.jpg"

                val folderPath = Environment.getExternalStorageDirectory().absolutePath +
                    File.separator + "SwiftAddress" + File.separator

                if (!File(folderPath).exists()) {
                    File(folderPath).mkdir()
                }

                val file = File(folderPath + fileName)

                Log.i(DEBUG_TAG, "filePath: ${folderPath + fileName}")

                val bitmap = bundle.get("data") as Bitmap

                var exif : ExifInterface? = null


                val fileOutputStream = FileOutputStream(file, false)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()

                exif = ExifInterface(file.canonicalPath)

                Log.i(DEBUG_TAG, "latitude: ${map.mapCenter.latitude.toString()}")

                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
                        map.mapCenter.latitude.toString())
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
                        map.mapCenter.longitude.toString())
                exif.saveAttributes()



                Log.i(DEBUG_TAG, "Image saved.")

            } else {
                Log.e(DEBUG_TAG, "Bundle was not null")
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT)
                        .show()
            }
*/
        // Save image to internal storage
        else if (requestCode == 4 && resultCode == RESULT_OK) {
            try {
                val exif = ExifInterface(File(currentImagePath!!))
                Log.i(TAG, "filePath: $currentImagePath")

                val latitude = map.mapView.mapCenter.latitude
                Log.i("lat", "$latitude")
                val latitudeHours = if (latitude > 0) latitude else (-1) * latitude // -105.9876543 -> 105.9876543
                var trueLat = latitudeHours.toInt().toString() + "/1," // 105/1,
                val latitudeMinutes = (latitudeHours % 1) * 60 // .987654321 * 60 = 59.259258
                trueLat = trueLat + latitudeMinutes.toInt().toString() + "/1," // 105/1,59/1,
                val latitudeSeconds = (latitudeMinutes % 1) * 60000 // .259258 * 6000 = 1555
                trueLat = trueLat + latitudeSeconds.toInt().toString() + "/1000" // 105/1,59/1,15555/1000

                val longitude = map.mapView.mapCenter.longitude
                val longitudeHours = if (longitude > 0) longitude else (-1) * longitude // -105.9876543 -> 105.9876543
                var trueLon = longitudeHours.toInt().toString() + "/1," // 105/1,
                val longitudeMinutes = (longitudeHours % 1) * 60 // .987654321 * 60 = 59.259258
                trueLon = trueLon + longitudeMinutes.toInt().toString() + "/1," // 105/1,59/1,
                val longitudeSeconds = (longitudeMinutes % 1) * 60000 // .259258 * 6000 = 1555
                trueLon = trueLon + longitudeSeconds.toInt().toString() + "/1000" // 105/1,59/1,15555/100

                Log.i(TAG, "lat: $latitude, lon: $longitude")
                Log.i(TAG, "trueLat: $trueLat, trueLong: $trueLon")

                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, trueLat)
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, trueLon)
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,
                        if (latitude > 0) "N" else "S")
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,
                        if (longitude > 0) "E" else "W")


                exif.saveAttributes()

                Log.i("exif latitude",
                        exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE).toString())
                Log.i("exif longitude",
                        exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE).toString())
                Log.i("Exif latituderef",
                        exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF).toString())
                Log.i("exif longituderef",
                        exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF).toString())
/*
                markerList.add(Marker(map))

                markerList.last().position = GeoPoint(latitude, longitude)
                markerList.last().icon = ContextCompat.getDrawable(this, R.drawable.camera)
                markerList.last().setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                // TODO : Show image when marker is clicked.

                map.overlays.add(markerList.last())
                Log.i(DEBUG_TAG, "Image Marker Added to Map")
*/
                val imageID = storeHouseNumbersObject.addImage(currentImagePath!!, latitude, longitude)

                if (imageID != -1L) {
                    map.addImageMarker(imageID, latitude, longitude)
                    Log.i(TAG, "Image Marker added to map")
                } else {
                    Toast.makeText(this, getString(R.string.failed_save),
                            Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        // Saves data
        else if (requestCode == 5 && resultCode == RESULT_OK) {

            Log.i(TAG, "attempting to store zip file.")

            val (i, j) = storeHouseNumbersObject.writeToOsmFile()

            Log.i(TAG, "Addresses and Notes written to XML file in internal storage.")
            val savingFilesDialog = AlertDialog.Builder(this)
            savingFilesDialog.setTitle(getString(R.string.saving_files))
            savingFilesDialog.setMessage("Please wait...")
            savingFilesDialog.setCancelable(false)
            val dialog = savingFilesDialog.create()

            Thread {
                try {
                    runOnUiThread {
                        dialog.show()
                    }
                    // create zip file, delete app internal storage, store zip file to chosen location.
                    val k = storeHouseNumbersObject.zipFilesAndDelete(data?.data!!)
                    Log.i(TAG, "Data has been zipped and stored")


                    runOnUiThread {
                        // clear markers
                        map.removeAllMarkers()
                        dialog.dismiss()
                        Log.i(TAG, "Database cleared, markers removed & dialog dismissed.")

                        if (i != -1 || j != -1 || k != 0) {
                            // clear database
                            storeHouseNumbersObject.clearDatabase()
                            Log.i(TAG, "Database cleared.")

                            // Dialogue showing number of objects saved
                            val dataSavedDialog = AlertDialog.Builder(this)
                            dataSavedDialog.setTitle(getString(R.string.data_saved))
                            dataSavedDialog.setMessage(
                                getString(R.string.addresses_saved) + " "
                                        + (abs(i) - 1).toString() + "\n"
                                        + getString(R.string.notes_saved) + " "
                                        + (abs(j) - 1).toString() + "\n"
                                        + getString(R.string.images_saved) + " " + k
                            )
                            dataSavedDialog.setPositiveButton(getString(R.string.close)) { _, _ -> }

                            dataSavedDialog.setOnDismissListener {
                                // Intent is restarted to fix a few bugs that show up when
                                // data is saved. You cannot rotate the map & add notes using
                                // a long press when the map is saved.
                                // Restarting the intent fixes this.
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            dataSavedDialog.create().show()
                        } else {
                            Toast.makeText(this, getString(R.string.osm_files_empty),
                                Toast.LENGTH_SHORT).show()
                            // Intent is restarted to fix a few bugs that show up when
                            // data is saved. You cannot rotate the map & add notes using
                            // a long press when the map is saved.
                            // Restarting the intent fixes this.
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this,
                                getString(R.string.failed_save), Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    Log.i(TAG, "Dialog dismissed")
                }
            }.start()
        }
        // Opens audio application when user asks to record audio. Currently unused.
        else if (requestCode == 6 && resultCode == RESULT_OK) {
            try {
                val exif = ExifInterface(File(currentAudioPath!!))
                Log.i(TAG, "filePath: $currentAudioPath")

                val latitude = map.mapView.mapCenter.latitude
                Log.i("lat", "$latitude")
                val latitudeHours = if (latitude > 0) latitude else (-1) * latitude // -105.9876543 -> 105.9876543
                var trueLat = latitudeHours.toInt().toString() + "/1," // 105/1,
                val latitudeMinutes = (latitudeHours % 1) * 60 // .987654321 * 60 = 59.259258
                trueLat = trueLat + latitudeMinutes.toInt().toString() + "/1," // 105/1,59/1,
                val latitudeSeconds = (latitudeMinutes % 1) * 60000 // .259258 * 6000 = 1555
                trueLat = trueLat + latitudeSeconds.toInt().toString() + "/1000" // 105/1,59/1,15555/1000

                val longitude = map.mapView.mapCenter.longitude
                val longitudeHours = if (longitude > 0) longitude else (-1) * longitude // -105.9876543 -> 105.9876543
                var trueLon = longitudeHours.toInt().toString() + "/1," // 105/1,
                val longitudeMinutes = (longitudeHours % 1) * 60 // .987654321 * 60 = 59.259258
                trueLon = trueLon + longitudeMinutes.toInt().toString() + "/1," // 105/1,59/1,
                val longitudeSeconds = (longitudeMinutes % 1) * 60000 // .259258 * 6000 = 1555
                trueLon = trueLon + longitudeSeconds.toInt().toString() + "/1000" // 105/1,59/1,15555/100

                Log.i(TAG, "lat: $latitude, lon: $longitude")
                Log.i(TAG, "trueLat: $trueLat, trueLong: $trueLon")

                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, trueLat)
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, trueLon)
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,
                        if (latitude > 0) "N" else "S")
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,
                        if (longitude > 0) "E" else "W")


                exif.saveAttributes()

                markerList.add(Marker(map.mapView))

                markerList.last().position = GeoPoint(latitude, longitude)
                markerList.last().icon = ContextCompat.getDrawable(this, R.drawable.audio)
                markerList.last().setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                // TODO : Show image when marker is clicked.
                map.mapView.overlays.add(markerList.last())
                Log.i(TAG, "Audio Marker Added to Map")

                storeHouseNumbersObject.addImage(currentAudioPath!!, latitude, longitude)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        // After settings have been closed.
        else if (requestCode == 7 && resultCode == RESULT_OK) {
            val sp = PreferenceManager.getDefaultSharedPreferences(this)

            if (sp.getBoolean("screen_timeout", false)) {
                Log.i(TAG, "screen is being kept on")
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                Log.i(TAG, "screen is no longer being kept on")
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            if (sp.getBoolean("clear_cache", false)) {

            }

            if (sp.getString("interface", "Default") == "Classic") {
                val intent = Intent(this, ClassicMainActivity::class.java)

                startActivity(intent)
                finish()
            }
        }
        // After Addresses have been downloaded from drawer button to "Download Addresses"
        else if (requestCode == 8 && resultCode == RESULT_OK) {
            restart()
        }

    }


    // Function that switches imageries based on what was chosen in
    // "ChangeBackgroundImagery" fragment
    private fun backgroundImagery() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        imagery = sp.getString("imagery", "Osm Carto").toString()

        Log.i(TAG, "Attempting to change background imagery")
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

        val mapboxSatellite = XYTileSource("mapbox",
                0,
                17,
                256,
                ".jpg?access_token=pk.eyJ1Ijoib3BlbnN0cmVldG1hcCIsImEiOiJja2w5YWt5bnYwNjZmMnFwZjhtbHk1MnA1In0.eq2aumBK6JuRoIuBMm6Gew",
                arrayOf(
                        "https://a.tiles.mapbox.com/v4/mapbox.satellite/",
                        "https://b.tiles.mapbox.com/v4/mapbox.satellite/",
                        "https://c.tiles.mapbox.com/v4/mapbox.satellite/",
                        "https://d.tiles.mapbox.com/v4/mapbox.satellite/")
        )

        val bingSatellite = XYTileSource("bing",
                0,
                17,
                256,
                "",
                arrayOf("https://bing.com/maps/")
        )

        val esriSatellite = object:OnlineTileSourceBase("esri",
                0,
                17,
                256,
                "",
                arrayOf(
                        "https://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/",
                        "https://server.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/",
                )) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                val url = baseUrl + MapTileIndex.getZoom(pMapTileIndex) +
                        "/" + MapTileIndex.getY(pMapTileIndex) +
                        "/" + MapTileIndex.getX(pMapTileIndex)
                return url
            }
        }


        // Switch statement to set tile source based on what string was given.
        when (imagery) {
            "Osm Carto" -> {
                map.mapView.setTileSource(mapnik)
                findViewById<ImageView>(R.id.crosshair).imageTintList =
                    ColorStateList.valueOf(Color.BLACK)
            }
            "Mapbox Satellite" -> {
                map.mapView.setTileSource(mapboxSatellite)
                findViewById<ImageView>(R.id.crosshair).imageTintList =
                    ColorStateList.valueOf(Color.RED)
            }
            "Bing Satellite" -> {
                map.mapView.setTileSource(bingSatellite)
                findViewById<ImageView>(R.id.crosshair).imageTintList =
                    ColorStateList.valueOf(Color.RED)
            }
            "Esri Satellite" -> {
                map.mapView.setTileSource(esriSatellite)
                findViewById<ImageView>(R.id.crosshair).imageTintList =
                    ColorStateList.valueOf(Color.RED)
            }
            "Custom" -> {
                try {
                    map.mapView.setTileSource(tmsToSlippy(
                            sp.getString("custom-imagery", "")!!
                    ))
                    findViewById<ImageView>(R.id.crosshair).imageTintList =
                        ColorStateList.valueOf(Color.RED)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, getString(R.string.invalid_imagery),
                            Toast.LENGTH_SHORT).show()
                    map.mapView.setTileSource(mapnik)
                    sp.edit().putString("imagery", "Osm Carto").apply()
                    findViewById<ImageView>(R.id.crosshair).imageTintList =
                        ColorStateList.valueOf(Color.BLACK)
                }
            }
        }

    }

    fun tmsToSlippy(tmsUrl : String) : OnlineTileSourceBase? {

        var runCondition = true

        try {
            val xIndex = tmsUrl.indexOf("{x}")
            val yIndex = tmsUrl.indexOf("{y}")
            var zoomIndex = tmsUrl.indexOf("{zoom}")

            if (zoomIndex == -1) {
                zoomIndex = tmsUrl.indexOf("{z}")
            }

            if (xIndex == -1 || yIndex == -1 || zoomIndex == -1) {
                throw NullPointerException("x, y or zoom clause not found")
            }

            Log.i(TAG, "xIndex : $xIndex")
            Log.i(TAG, "yIndex : $yIndex")

        } catch (e : NullPointerException) {
            e.printStackTrace()
            Log.w(TAG, e.message.toString())
            Log.w(TAG, e.cause.toString())
            runCondition = false
        }
        val switchRegex = Regex("\\{switch:.+\\}")
        var switchIndex = -1
        var switchValue : String? = null
        var startString : String? = null
        val startStringList = mutableListOf<String>()
        try {
            switchIndex = switchRegex.find(tmsUrl)!!.range.last
            switchValue = switchRegex.find(tmsUrl)!!.value
            Log.i(TAG, "switchValue : $switchValue")
            Log.i(TAG, "switchIndex : $switchIndex")

            startString = tmsUrl.substring(0, switchIndex)

            switchValue = switchValue.removePrefix("{switch:")
            switchValue = switchValue.removeSuffix("}")
            val valuesList = switchValue.split(",")

            for (value in valuesList) {
                val stringToAdd = startString.replace("\\{switch:.+\\}", value)
                Regex("tms\\[.+\\]").find(stringToAdd)?.range?.let { stringToAdd.removeRange(it) }
                Log.i(TAG, "stringToAdd: $stringToAdd")
                startStringList.add(stringToAdd)
            }
        } catch (e : Exception) {
            e.printStackTrace()
            Log.w(TAG, "No switch clause")

            switchIndex = -1
            switchValue = null
            startString = null
        }



        if (runCondition) {
            val tileSource = object : OnlineTileSourceBase("custom",
                    0,
                    17,
                    256,
                    "",
                    startStringList.toTypedArray()
            ) {
                override fun getTileURLString(pMapTileIndex: Long): String {
                    var string =
                            if (switchIndex != -1) tmsUrl.substring(switchIndex + 1)
                            else tmsUrl

                    Log.i(TAG, "TileURLString: $string")
                    string = string.replace("{x}", MapTileIndex.getX(pMapTileIndex).toString())
                    string = string.replace("{y}", MapTileIndex.getY(pMapTileIndex).toString())
                    string = string.replace("{zoom}", MapTileIndex.getZoom(pMapTileIndex).toString())
                    return baseUrl + string + mImageFilenameEnding
                }
            }
            return tileSource
        } else {
            Log.w(TAG, "startString: ${startString}")
            return null

        }


        /*
        val xValsRegex = Regex("\\{x\\}")
        val xValsIndexes : List<Int> = xValsRegex.findAll(tmsUrl).map { it.range.first }.toList()
        val yValsRegex = Regex("\\{y\\}")
        val yValsIndexes : List<Int> = yValsRegex.findAll(tmsUrl).map { it.range.first }.toList()
        val switchValsRegex = Regex("\\{switch:")
        val switchValsIndexes : List<Int> = switchValsRegex.findAll(tmsUrl).map {
            it.range.first }.toList()
        
        var runCondition = true

        if (switchValsIndexes.size > 1) {
            runCondition = false
        }

        var startString = ""
        var endString = ""

        var midStartIndex = 0
        var midEndIndex = tmsUrl.length - 1

        val mutableList = mutableListOf<String>()
        if (xValsIndexes.isNotEmpty() && yValsIndexes.isNotEmpty()) {

            for (switchValIndex in switchValsIndexes) {
                for (xValIndex in xValsIndexes) {
                    if (switchValIndex > xValIndex) {
                        runCondition = false
                    }
                }
                for (yValIndex in yValsIndexes) {
                    if (switchValIndex > yValIndex) {
                        runCondition = false
                    }
                }
            }

            if (xValsIndexes.minOrNull()!! > yValsIndexes.minOrNull()!!) {
                startString = tmsUrl.substring(0, yValsIndexes.minOrNull()!!)
                midStartIndex = yValsIndexes.minOrNull()!!
            } else {
                startString = tmsUrl.substring(0, xValsIndexes.minOrNull()!!)
                midStartIndex = xValsIndexes.minOrNull()!!
            }

            if (xValsIndexes.maxOrNull()!! < yValsIndexes.maxOrNull()!!) {
                endString = tmsUrl.substring(yValsIndexes.maxOrNull()!! + 3)
                midEndIndex = yValsIndexes.maxOrNull()!! + 3
            } else {
                endString = tmsUrl.substring(xValsIndexes.maxOrNull()!! + 3)
                midEndIndex = yValsIndexes.maxOrNull()!! + 3
            }

            val switchValsRegex2 = Regex("\\{switch:.+\\}")
            val switchVals = switchValsRegex.findAll(startString).map { it.value }.toList()
            try {
                val switchVal = switchVals[0]
                val switchValOptions = switchVal.substring(8, switchVal.length - 1)
                val switchValOptionsList = switchValOptions.split(",")
                for(option in switchValOptionsList) {
                    val formattedString = startString.replace("\\{switch.+\\}", option)
                    mutableList.add(formattedString)
                }
            } catch (e : IndexOutOfBoundsException) {
                Log.i(TAG, "no Switch Clause")
                mutableList.add(startString)
            }
        } else {
            runCondition = false
        }
        if (runCondition) {
            val tileSource = object : OnlineTileSourceBase("custom",
                    0,
                    17,
                    256,
                    endString,
                    mutableList.toTypedArray()
            ) {
                override fun getTileURLString(pMapTileIndex: Long): String {
                    var midString = tmsUrl.substring(midStartIndex, midEndIndex)
                    midString = midString.replace("{x}", MapTileIndex.getX(pMapTileIndex).toString())
                    midString = midString.replace("{y}", MapTileIndex.getY(pMapTileIndex).toString())
                    midString = midString.replace("{zoom}", MapTileIndex.getZoom(pMapTileIndex).toString())
                    return baseUrl + midString + mImageFilenameEnding
                }
            }
            return tileSource
        } else {
            Log.w(TAG, "startString: ${startString} endString: ${endString}")
            return null
        }

         */
    }



    // If undo button is pressed
    fun undo(view: View) {
        if (map.undo()) {
            if (storeHouseNumbersObject.lastItemType() == "Image") {
                Log.i(TAG, "Last Item was an image.")
                val deleteFileDialog = AlertDialog.Builder(this)

                deleteFileDialog.setPositiveButton(getString(R.string.delete)) { _, _ ->
                    storeHouseNumbersObject.undo(true)
                }

                deleteFileDialog.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
                deleteFileDialog.setTitle(getString(R.string.delete_image))
                deleteFileDialog.setMessage(getString(R.string.delete_image_question))
                deleteFileDialog.create().show()
            } else {
                storeHouseNumbersObject.undo(false)
            }
        } else {
            Toast.makeText(this, getString(R.string.no_markers), Toast.LENGTH_SHORT).show()
        }
    }

    // add a note to the map as a marker and to the database.
    private fun addNote(lat: Double, lon: Double) {
        Log.i(TAG, "addNote() method started")
        val vibrator: Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        vibrate(80, VibrationEffect.DEFAULT_AMPLITUDE)

        val addNoteBuilder = AlertDialog.Builder(this)
        addNoteBuilder.setTitle(getString(R.string.add_note))

        var noteContents: String
        val note = EditText(this)
        note.minLines = 5
        val container = FrameLayout(this)
        val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
                MATCH_PARENT, WRAP_CONTENT
        )
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        note.layoutParams = params
        note.gravity = Gravity.TOP

        container.addView(note)


        addNoteBuilder.setView(container)

        addNoteBuilder.setPositiveButton(getString(R.string.save_note)) { _, _ ->
            noteContents = note.text.toString()

            Log.i(TAG, "noteContents: $noteContents")
            if (noteContents != "") {
                val noteID = storeHouseNumbersObject.addNote(noteContents, lat, lon)
                if (noteID != 1L) {
                    map.addNoteMarker(noteID, lat, lon, noteContents)
                } else {
                    Toast.makeText(this, R.string.failed_save, Toast.LENGTH_SHORT).show()
                }



                Log.i("button press", "setPositiveButton pressed")
            }
        }

        addNoteBuilder.setNeutralButton(getString(R.string.cancel)) { _, _ -> }

        val dialog = addNoteBuilder.create()
        dialog.show()
        note.requestFocus()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
   }

    // Save all the data collected to an .osm file, clear markers
    fun saveData(view: View) {

        val saveDataDialogue  = AlertDialog.Builder(this)
        saveDataDialogue.setTitle(getString(R.string.save_data))
        saveDataDialogue.setMessage(getString(R.string.save_data_question))

        saveDataDialogue.setNeutralButton("Cancel") { _, _ -> }

        saveDataDialogue.setPositiveButton(getString(R.string.save)) { _, _ ->

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/zip"
            val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)
            Log.i(TAG, "locale: $locale")
            val date = SimpleDateFormat("yyyy-mm-dd-hh-mm-ss", locale).format(Date())
            intent.putExtra(Intent.EXTRA_TITLE, "survey-$date.zip")

            startActivityForResult(intent, 5)
        }

        saveDataDialogue.create().show()
        // storeHouseNumbersObject.writeToOsmFile()


    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent?) {}

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.i(TAG, "onLongPress")
        longPressDetected = true
    }

    override fun onFling(
            downEvent: MotionEvent,
            moveEvent: MotionEvent,
            velocityX: Float,
            velocityY: Float
    ): Boolean {
        Log.i(TAG, "onFling")
        val swipeValue = 100

        val differenceInY = moveEvent.y - downEvent.y
        val differenceInX = moveEvent.x - downEvent.x

        if (abs(differenceInY) > abs(differenceInX)
                && abs(differenceInY) > swipeValue
                && differenceInY < 0
        ) {
            // This is an up swipe
            flingUpDetected = true
        }

        if (abs(differenceInX) > abs(differenceInY)) {
            Log.i(TAG, "true")
            if (abs(differenceInX) > swipeValue && differenceInX > 0) {
                flingRightDetected = true
            } else if (abs(differenceInX) > swipeValue && differenceInX < 0) {
                flingLeftDetected = true
            } else {
                Log.w(TAG, "Swipe Distance was too small")
            }
        }
        return true
    }

    // changes the "side" of the swipe up keypad. For example, swiping from right
    // when on the "left side" swipe up keypad would move you to the
    // "right side" swipe up keypad.
    @SuppressLint("SetTextI18n")
    private fun switchSwipeUpKeypadSide(isOnLeft : Boolean) {

        Log.w(TAG, "switching swipe up keypad side")
        val houseNumberTextView = findViewById<TextView>(R.id.mini_keypad_housenumber)
        try {
            val address =
                if (isOnLeft) storeHouseNumbersObject.lastAddressEntry("left")
                else storeHouseNumbersObject.lastAddressEntry("right")

            houseNumberTextView.text = incrementAddress(address!!.housenumber)

            val swipeUpRelativeLayout = findViewById<RelativeLayout>(R.id.swipe_up_relative_layout)
            swipeUpRelativeLayout.background =
                if (isOnLeft) ContextCompat.getDrawable(this, R.drawable.left_gradient)
                else ContextCompat.getDrawable(this, R.drawable.right_gradient)

            val sideTextView = findViewById<TextView>(R.id.mini_keypad_side)
            sideTextView.text =
                    if (isOnLeft) getString(R.string.left_side)
                    else getString(R.string.right_side)

            val button1 = findViewById<ImageButton>(R.id.B1R0_mini_relative)
            val button2 = findViewById<ImageButton>(R.id.B2R0_mini_relative)
            val button3 = findViewById<ImageButton>(R.id.B3R0_mini_relative)
            val button4 = findViewById<ImageButton>(R.id.B1R1_mini_relative)
            val button5 = findViewById<ImageButton>(R.id.B2R1_mini_relative)
            val button6 = findViewById<ImageButton>(R.id.B3R1_mini_relative)

            if (isOnLeft) {
                button1.tag = "left"
                button2.tag = "left"
                button3.tag = "left"
                button4.tag = "left"
                button5.tag = "left"
                button6.tag = "left"
            } else {
                button1.tag = "right"
                button2.tag = "right"
                button3.tag = "right"
                button4.tag = "right"
                button5.tag = "right"
                button6.tag = "right"
            }
        } catch (e : NullPointerException) {
            e.printStackTrace()
            Toast.makeText(this, R.string.add_address_first, Toast.LENGTH_SHORT).show()
        }


    }

    // shows the swipe up keypad
    private fun showSwipeUpKeypad(isOnLeft : Boolean) {
        val addressToChange : AddressNodes? =
                if (isOnLeft) storeHouseNumbersObject.lastAddressEntry("left")
                else storeHouseNumbersObject.lastAddressEntry("right")

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        increment = sp.getInt("increment", 2)

        if (addressToChange != null) {
            Log.i(TAG, "flingUpDetected & address is not null")
            val houseNumber = findViewById<TextView>(R.id.mini_keypad_housenumber)

            addressToChange.housenumber = incrementAddress(addressToChange.housenumber)
            houseNumber.text = addressToChange.housenumber

            val miniKeypadSide = findViewById<TextView>(R.id.mini_keypad_side)
            miniKeypadSide.text =
                    if (isOnLeft) getString(R.string.left_side)
                    else getString(R.string.right_side)

            val swipeUpRelativeLayout =
                    findViewById<RelativeLayout>(R.id.swipe_up_relative_layout)
            swipeUpRelativeLayout.background =
                    if(isOnLeft) ContextCompat.getDrawable(this,
                            R.drawable.left_gradient)
                    else ContextCompat.getDrawable(this, R.drawable.right_gradient)

            val button1 = findViewById<ImageButton>(R.id.B1R0_mini_relative)
            val button2 = findViewById<ImageButton>(R.id.B2R0_mini_relative)
            val button3 = findViewById<ImageButton>(R.id.B3R0_mini_relative)
            val button4 = findViewById<ImageButton>(R.id.B1R1_mini_relative)
            val button5 = findViewById<ImageButton>(R.id.B2R1_mini_relative)
            val button6 = findViewById<ImageButton>(R.id.B3R1_mini_relative)

            button1.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.darker_gray)
            )
            button2.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.darker_gray)
            )
            button3.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.darker_gray)
            )
            button4.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.darker_gray)
            )
            button5.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.darker_gray)
            )
            button6.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.darker_gray)
            )

            Log.i(TAG, "buildingLevels, ${addressToChange.buildingLevels}")
            when (addressToChange.buildingLevels) {
                "B1 R0" -> button1.backgroundTintList =
                        ColorStateList.valueOf(
                                ContextCompat.getColor(
                                        this,
                                        android.R.color.holo_green_dark
                                )
                        )
                "B2 R0" -> button2.backgroundTintList =
                        ColorStateList.valueOf(
                                ContextCompat.getColor(
                                        this,
                                        android.R.color.holo_green_dark
                                )
                        )
                "B3 R0" -> button3.backgroundTintList =
                        ColorStateList.valueOf(
                                ContextCompat.getColor(
                                        this,
                                        android.R.color.holo_green_dark
                                )
                        )
                "B1 R1" -> button4.backgroundTintList =
                        ColorStateList.valueOf(
                                ContextCompat.getColor(
                                        this,
                                        android.R.color.holo_green_dark
                                )
                        )
                "B2 R1" -> button5.backgroundTintList =
                        ColorStateList.valueOf(
                                ContextCompat.getColor(
                                        this,
                                        android.R.color.holo_green_dark
                                )
                        )
                "B3 R1" -> button6.backgroundTintList =
                        ColorStateList.valueOf(
                                ContextCompat.getColor(
                                        this,
                                        android.R.color.holo_green_dark
                                )
                        )
            }

            if (isOnLeft) {
                button1.tag = "left"; button2.tag = "left"; button3.tag = "left"
                button4.tag = "left"; button5.tag = "left"; button6.tag = "left"
            } else {
                button1.tag = "right"; button2.tag = "right"; button3.tag = "right"
                button4.tag = "right"; button5.tag = "right"; button6.tag = "right"
            }
            slideUp.show()
        } else {
            Toast.makeText(this, getString(R.string.add_address_first),
                    Toast.LENGTH_SHORT).show()
        }
    }

    fun showSwipeUpKeypadPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.inflate(R.menu.mini_keypad_menu)
        popupMenu.show()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.change_increment_mini_keypad -> {
                modifyIncrement()
                return true
            }
            R.id.add_house_name_mini_keypad -> {
                addHouseName()
                return true
            }
            else -> {
                return false
            }
        }

    }

    private fun addHouseName() {
        val addHouseNameDialog = AlertDialog.Builder(this)
        addHouseNameDialog.setTitle(getString(R.string.house_name))

        var houseNameValue : String

        val addHouseNameEditText = EditText(this)
        addHouseNameEditText.maxLines = 1
        addHouseNameEditText.inputType = InputType.TYPE_CLASS_TEXT
        addHouseNameEditText.append(houseName)

        val container = FrameLayout(this)
        val params : FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            MATCH_PARENT, WRAP_CONTENT
        )
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        addHouseNameEditText.layoutParams = params
        container.addView(addHouseNameEditText)

        addHouseNameDialog.setView(container)

        addHouseNameDialog.setPositiveButton(getString(R.string.add_house_name)) {
            _, _ ->
            houseName = addHouseNameEditText.text.toString()
        }
        addHouseNameDialog.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
        val dialog = addHouseNameDialog.create()
        dialog.show()
        addHouseNameEditText.requestFocus()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

    }

    private fun modifyIncrement() {
        val incrementButtonDimensions = 200
        val textBoxWidth = 200
        val textBoxTextSize = 40f

        val sp = PreferenceManager.getDefaultSharedPreferences(this)

        increment = sp.getInt("increment", 2)

        Log.i(TAG, "Increment before function: $increment")
        val modifyIncrementDialogue = AlertDialog.Builder(this)
        modifyIncrementDialogue.setTitle(getString(R.string.change_increment))

        var incrementValue: String

        val modifyIncrementInput = EditText(this)
        modifyIncrementInput.inputType = EditorInfo.TYPE_CLASS_PHONE
        modifyIncrementInput.setText(increment.toString())
        modifyIncrementInput.textSize = textBoxTextSize
        modifyIncrementInput.layoutParams = ViewGroup.LayoutParams(textBoxWidth, MATCH_PARENT)
        modifyIncrementInput.gravity = CENTER

        val minusButton = ImageButton(this)
        minusButton.setImageResource(R.drawable.minus)

        minusButton.layoutParams = ViewGroup.LayoutParams(
                incrementButtonDimensions,
                incrementButtonDimensions
        )
        minusButton.setOnClickListener {
            val text = modifyIncrementInput.text.toString()
            if (text != "") {
                modifyIncrementInput.setText(incrementAddress(text, -1))
            }
            Log.i(TAG, "modifyIncrementDialog: minusButton pressed")
        }

        val plusButton = ImageButton(this)
        plusButton.setImageResource(R.drawable.plus)
        plusButton.layoutParams = ViewGroup.LayoutParams(
                incrementButtonDimensions,
                incrementButtonDimensions
        )
        plusButton.setOnClickListener {
            val text = modifyIncrementInput.text.toString()
            if (text != "") {
                modifyIncrementInput.setText(incrementAddress(text, 1))
            }
            Log.i(TAG, "modifyIncrementDialog: plusButton pressed")
        }

        val linearLayout = LinearLayout(this)

        linearLayout.layoutParams = ActionBar.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.gravity = CENTER
        linearLayout.addView(minusButton)
        linearLayout.addView(modifyIncrementInput)
        linearLayout.addView(plusButton)

        modifyIncrementDialogue.setView(linearLayout)
        modifyIncrementDialogue.setMessage(getString(R.string.increment_explanation))

        modifyIncrementDialogue.setPositiveButton(getString(R.string.save_increment)) { _, _ ->
            incrementValue = modifyIncrementInput.text.toString()
            try {
                Log.i(TAG, "incrementValue in Dialog: $incrementValue")
                increment = incrementValue.toInt()

                val sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                sharedPreferencesEditor.putInt("increment", increment)
                sharedPreferencesEditor.apply()
            } catch (e: TypeCastException) {
                e.printStackTrace()
                Log.e(TAG, getString(R.string.increment_not_integer))
                Toast.makeText(
                        this, getString(R.string.increment_not_integer),
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
        modifyIncrementDialogue.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
        modifyIncrementDialogue.create().show()
    }

    @SuppressLint("InlinedApi")
    fun swipeAddHouseNumber(view: View) {
        val buildingLevelsButton = findViewById<ImageButton>(view.id)
        val relativeLayout = buildingLevelsButton.parent as RelativeLayout
        val buildingLevelsTextView = relativeLayout.getChildAt(1) as TextView
        val houseNumber = findViewById<TextView>(R.id.mini_keypad_housenumber)

        Thread {
            vibrate(80, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrate(80, VibrationEffect.DEFAULT_AMPLITUDE)
        }.start()

        Log.i(TAG, "buildingLevels: ${buildingLevelsTextView.text}")
        val addressToChange = storeHouseNumbersObject.lastAddressEntry(
                buildingLevelsButton.tag.toString()
        )

        addressToChange!!.buildingLevels = buildingLevelsTextView.text.toString()
        addressToChange.latitude = map.mapView.mapCenter.latitude
        addressToChange.longitude = map.mapView.mapCenter.longitude
        addressToChange.housenumber = houseNumber.text.toString()
        addressToChange.houseName = houseName

        houseName = ""

        val id = storeHouseNumbersObject.addHouseNumber(addressToChange)
        if (id != -1L) {
            map.addHousenumberMarker(addressToChange, id)
        } else {
            Toast.makeText(this, R.string.failed_save, Toast.LENGTH_SHORT).show()
        }


        val button1 = findViewById<ImageButton>(R.id.B1R0_mini_relative)
        val button2 = findViewById<ImageButton>(R.id.B2R0_mini_relative)
        val button3 = findViewById<ImageButton>(R.id.B3R0_mini_relative)
        val button4 = findViewById<ImageButton>(R.id.B1R1_mini_relative)
        val button5 = findViewById<ImageButton>(R.id.B2R1_mini_relative)
        val button6 = findViewById<ImageButton>(R.id.B3R1_mini_relative)

        button1.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, android.R.color.darker_gray)
        )
        button2.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, android.R.color.darker_gray)
        )
        button3.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, android.R.color.darker_gray)
        )
        button4.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, android.R.color.darker_gray)
        )
        button5.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, android.R.color.darker_gray)
        )
        button6.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, android.R.color.darker_gray)
        )

        Log.i(TAG, "buildingLevels, ${addressToChange.buildingLevels}")
        buildingLevelsButton.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, android.R.color.holo_green_dark)
        )

        Log.i(TAG, "flingUpDetected & address is not null")

        // val sharedPreferences = getSharedPreferences(getString(R.string.preference_string), MODE_PRIVATE)
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        increment = sp.getInt("increment", 2)

        houseNumber.text = incrementAddress(houseNumber.text.toString())
        map.mapView.invalidate()
    }

    fun decrementMiniHousenumber(view: View) {
        val houseNumber = findViewById<TextView>(R.id.mini_keypad_housenumber)
        houseNumber.text = incrementAddress(houseNumber.text.toString(), -1)


    }

    fun incrementMiniHousenumber(view: View) {

        val houseNumber = findViewById<TextView>(R.id.mini_keypad_housenumber)
        houseNumber.text = incrementAddress(houseNumber.text.toString(), 1)
    }

    fun closeMiniKeypad(view: View) {
        slideUp.hide()
    }

    fun takePhoto(view: View) {

        val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)
        val current = SimpleDateFormat("yyyy-mm-dd-hh-mm-ss", locale).format(Date())
        val fileName = "image-$current.jpg"

        val imageFile = File(getExternalFilesDir("data"), fileName)
        currentImagePath = imageFile.absolutePath

        Log.i(TAG, "filePath: ${getExternalFilesDir("data")!!.absolutePath + fileName}")
        Log.i(TAG, "filePath: $currentImagePath")
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {

            val imageURI : Uri = FileProvider.getUriForFile(this,
                    "com.mapitall.SwiftAddress.provider",
                    imageFile
            )
            Log.i(TAG, "URI filePath: $imageURI")
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
            startActivityForResult(cameraIntent, 4)
        } else {
            Toast.makeText(this, getString(R.string.no_camera_app),
                    Toast.LENGTH_SHORT).show()
        }
    }

    fun captureAudio(view: View) {

        Toast.makeText(this, getString(R.string.unimplemented),
                Toast.LENGTH_SHORT).show()

    }

    @SuppressLint("ClickableViewAccessibility")
    fun moveMarker(ID : Long, marker : Marker, downloaded: Boolean) {
        val moveButton = findViewById<Button>(R.id.move_marker)
        val cancelButton = findViewById<Button>(R.id.cancel_move_marker)
        val leftArrow = findViewById<ImageButton>(R.id.add_address_on_left)
        val rightArrow = findViewById<ImageButton>(R.id.add_address_on_right)

        map.markerToMove = marker
        map.moveMarkerCondition = true

        val oldPosition = marker.position
        moveButton.visibility = View.VISIBLE
        cancelButton.visibility = View.VISIBLE
        leftArrow.visibility = View.GONE
        rightArrow.visibility = View.GONE

        moveButton.setOnClickListener {
            map.moveMarkerCondition = false
            moveButton.visibility = View.GONE
            cancelButton.visibility = View.GONE
            leftArrow.visibility = View.VISIBLE
            rightArrow.visibility = View.VISIBLE

            if (downloaded) {
                map.changeDownloadedMarkerLocation(
                        ID, storeHouseNumbersObject.getHouseNumber(ID, true))
                storeHouseNumbersObject.changeDownloadedMarkerStatus(ID, "updated")
            }
            marker.position = map.mapView.mapCenter as GeoPoint
            map.mapView.invalidate()

            storeHouseNumbersObject.changeLocation(ID,
                    map.mapView.mapCenter.latitude,
                    map.mapView.mapCenter.longitude)
        }

        cancelButton.setOnClickListener {
            map.moveMarkerCondition = false
            moveButton.visibility = View.GONE
            cancelButton.visibility = View.GONE
            leftArrow.visibility = View.VISIBLE
            rightArrow.visibility = View.VISIBLE

            marker.position = oldPosition
            map.mapView.invalidate()

        }
    }

    fun createPolyline(
            polyline_: Polyline,
            geoPoints_: ArrayList<GeoPoint>,
            startMarkerID_: Long) {
        val addPointButton = findViewById<Button>(R.id.add_interpolation_way_point)
        val cancelButton = findViewById<Button>(R.id.cancel_interpolation_way)
        val leftArrow = findViewById<ImageButton>(R.id.add_address_on_left)
        val rightArrow = findViewById<ImageButton>(R.id.add_address_on_right)

        polyline = polyline_
        geoPoints = geoPoints_
        startMarkerID = startMarkerID_
        creatingInterpolationWay = true

        addPointButton.visibility = View.VISIBLE
        cancelButton.visibility = View.VISIBLE
        leftArrow.visibility = View.GONE
        rightArrow.visibility = View.GONE

        addPointButton.setOnClickListener {
            map.makeLineFollowCenter(false)
            geoPoints!!.add(map.mapView.mapCenter as GeoPoint)
            polyline!!.setPoints(geoPoints!!)
            map.makeLineFollowCenter(true)

        }
        cancelButton.setOnClickListener {
            map.makeLineFollowCenter(false)
            map.mapView.overlays.remove(polyline)
            map.mapView.invalidate()
            creatingInterpolationWay = false
            InfoWindow.closeAllInfoWindowsOn(map.mapView)

            addPointButton.visibility = View.GONE
            cancelButton.visibility = View.GONE
            leftArrow.visibility = View.VISIBLE
            rightArrow.visibility = View.VISIBLE
        }
    }
    fun finishCreatingInterpolationWay() {
        val addPointButton = findViewById<Button>(R.id.add_interpolation_way_point)
        val cancelButton = findViewById<Button>(R.id.cancel_interpolation_way)
        val leftArrow = findViewById<ImageButton>(R.id.add_address_on_left)
        val rightArrow = findViewById<ImageButton>(R.id.add_address_on_right)

        addPointButton.visibility = View.GONE
        cancelButton.visibility = View.GONE
        leftArrow.visibility = View.VISIBLE
        rightArrow.visibility = View.VISIBLE
    }

    fun openDrawer(view: View) {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.openDrawer(GravityCompat.START)
    }

    fun restart() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}

