package com.mapitall.SwiftAddress

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.ColorStateList
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
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
import androidx.core.view.GestureDetectorCompat
import androidx.exifinterface.media.ExifInterface
import androidx.preference.PreferenceManager
import com.mancj.slideup.SlideUp
import com.mancj.slideup.SlideUpBuilder
import layout.AddressNodes
import layout.StoreHouseNumbers
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.abs

class MainActivity : AppCompatActivity(),
    MapEventsReceiver,
    GestureDetector.OnGestureListener,
    PopupMenu.OnMenuItemClickListener {

    private var currentImagePath: String? = null
    private var DEBUG_TAG = "MainActivity"

    private lateinit var map: MapView
    private var markerList: MutableList<Marker> = mutableListOf()
    private var storeHouseNumbersObject: StoreHouseNumbers = StoreHouseNumbers(this)
    private var increment = 2
    private var noOnTouchActions = true
    private var flingUpDetected = false
    private var longPressDetected = false
    private lateinit var slideUp: SlideUp
    private val instandepic = this

    @SuppressLint("ClickableViewAccessibility")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set map details

        val sharedPreferences = getSharedPreferences(getString(R.string.preference_string), MODE_PRIVATE)
        increment = sharedPreferences.getInt("increment", 2)
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

        val gestureDetector = GestureDetectorCompat(this, this)

        // Zoom to current position when the app starts.
        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, false)
            val location = locationManager.getLastKnownLocation(provider!!)
            mapController.animateTo(GeoPoint(location!!.latitude, location.longitude))
            mapController.zoomTo(17, null)
            Log.i(DEBUG_TAG, "zoomed to location")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Ask for user permissions
        // TODO: improve this code so that it asks for external storage permission again
        //  if the user denied.
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


        val slideUpView = findViewById<View>(R.id.basedView)
        slideUp = SlideUpBuilder(
                slideUpView
        ).withStartState(SlideUp.State.HIDDEN)
                .withStartGravity(Gravity.BOTTOM)
                .build()

        leftArrow.setOnTouchListener { _, event ->
            Log.i(DEBUG_TAG, "leftArrow: onTouchListener Called")

            flingUpDetected = false

            gestureDetector.onTouchEvent(event)
            // IF YOU SWIPE UP ON THE LEFT ARROW
            if (flingUpDetected) {

                val addressToChange = storeHouseNumbersObject.lastAddressEntry("left")
                increment = sharedPreferences.getInt("increment", 2)


                if (addressToChange != null) {
                    Log.i(DEBUG_TAG, "flingUpDetected & address is not null")
                    val houseNumber = findViewById<TextView>(R.id.mini_keypad_housenumber)

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
                    houseNumber.text = addressToChange.housenumber
                    val miniKeypadSide = findViewById<TextView>(R.id.mini_keypad_side)
                    miniKeypadSide.text = getString(R.string.left_side)

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

                    Log.i(DEBUG_TAG, "buildingLevels, ${addressToChange.buildingLevels}")
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

                    button1.tag = "left"; button2.tag = "left"; button3.tag = "left"
                    button4.tag = "left"; button5.tag = "left"; button6.tag = "left"
                    slideUp.show()
                } else {
                    Toast.makeText(this, getString(R.string.add_address_first),
                            Toast.LENGTH_SHORT).show()
                }
            } else {
                noOnTouchActions = true
            }

            return@setOnTouchListener super.onTouchEvent(event)
        }


        leftArrow.setOnLongClickListener {

            val addressToChange = storeHouseNumbersObject.lastAddressEntry("left")
            increment = sharedPreferences.getInt("increment", 2)

            if (addressToChange != null) {
                Log.i(DEBUG_TAG, "longPressDetected & address is not null")
                val vibrator: Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator


                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                    150, VibrationEffect.DEFAULT_AMPLITUDE
                            )
                    )
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

                map.invalidate()
            } else {
                Toast.makeText(this, getString(R.string.add_address_first),
                        Toast.LENGTH_SHORT).show()
            }
            return@setOnLongClickListener true
        }

        leftArrow.setOnClickListener {
            Log.i(DEBUG_TAG, "onclickcalled")
            if (noOnTouchActions) {
                val coordinates = map.mapCenter
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

            if (flingUpDetected) {

                val addressToChange = storeHouseNumbersObject.lastAddressEntry("right")
                increment = sharedPreferences.getInt("increment", 2)


                if (addressToChange != null) {
                    Log.i(DEBUG_TAG, "flingUpDetected & address is not null")
                    val houseNumber = findViewById<TextView>(R.id.mini_keypad_housenumber)

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
                    houseNumber.text = addressToChange.housenumber
                    val miniKeypadSide = findViewById<TextView>(R.id.mini_keypad_side)
                    miniKeypadSide.text = getString(R.string.right_side)
                    Log.i(
                            DEBUG_TAG,
                            "backgroundTint, ${findViewById<ImageButton>(R.id.B1R0_mini_relative).backgroundTintList}"
                    )

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


                    Log.i(DEBUG_TAG, "buildingLevels, ${addressToChange.buildingLevels}")
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

                    button1.tag = "right"; button2.tag = "right"; button3.tag = "right"
                    button4.tag = "right"; button5.tag = "right"; button6.tag = "right"
                    slideUp.show()


                } else {
                    Toast.makeText(this, getString(R.string.add_address_first),
                            Toast.LENGTH_SHORT).show()
                }
            } else {
                noOnTouchActions = true
            }

            return@setOnTouchListener super.onTouchEvent(event)
        }

        val degreeLatitude = Location.convert(map.mapCenter.latitude, Location.FORMAT_DEGREES)
        Log.i(DEBUG_TAG, "degreeLatitude  $degreeLatitude")

        rightArrow.setOnLongClickListener {
            val addressToChange = storeHouseNumbersObject.lastAddressEntry("right")
            Log.e(DEBUG_TAG, "increment; $increment")
            increment = sharedPreferences.getInt("increment", 2)
            Log.e(DEBUG_TAG, "increment $increment")
            if (addressToChange != null) {
                Log.i(DEBUG_TAG, "longPressDetected & address is not null")
                val vibrator: Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                    150, VibrationEffect.DEFAULT_AMPLITUDE
                            )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(150)
                }
                try {
                    Log.e(DEBUG_TAG, "increment: $increment")
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

                map.invalidate()
            } else {
                Toast.makeText(this, getString(R.string.add_address_first),
                        Toast.LENGTH_SHORT).show()
            }
            return@setOnLongClickListener true

        }

        rightArrow.setOnClickListener {

            if (noOnTouchActions) {
                val coordinates = map.mapCenter
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

                Log.i(DEBUG_TAG, location!!.latitude.toString())
                if (map.zoomLevelDouble < 17) {
                    map.controller.zoomTo(17.0)
                }
                map.controller.animateTo(GeoPoint(location!!.latitude, location.longitude))

                Log.i(DEBUG_TAG, "zoomLevel: ${map.zoomLevelDouble}")
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

        for (marker: Marker in markerList) {
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
            Log.i("imagery chosen option: ", imageryChoice.toString())

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
                Log.i(DEBUG_TAG, "filePath: $currentImagePath")

                val latitude = map.mapCenter.latitude
                Log.i("lat", "$latitude")
                val latitudeHours = if (latitude > 0) latitude else (-1) * latitude // -105.9876543 -> 105.9876543
                var trueLat = latitudeHours.toInt().toString() + "/1," // 105/1,
                val latitudeMinutes = (latitudeHours % 1) * 60 // .987654321 * 60 = 59.259258
                trueLat = trueLat + latitudeMinutes.toInt().toString() + "/1," // 105/1,59/1,
                val latitudeSeconds = (latitudeMinutes % 1) * 60000 // .259258 * 6000 = 1555
                trueLat = trueLat + latitudeSeconds.toInt().toString() + "/1000" // 105/1,59/1,15555/1000

                val longitude = map.mapCenter.longitude
                val longitudeHours = if (longitude > 0) longitude else (-1) * longitude // -105.9876543 -> 105.9876543
                var trueLon = longitudeHours.toInt().toString() + "/1," // 105/1,
                val longitudeMinutes = (longitudeHours % 1) * 60 // .987654321 * 60 = 59.259258
                trueLon = trueLon + longitudeMinutes.toInt().toString() + "/1," // 105/1,59/1,
                val longitudeSeconds = (longitudeMinutes % 1) * 60000 // .259258 * 6000 = 1555
                trueLon = trueLon + longitudeSeconds.toInt().toString() + "/1000" // 105/1,59/1,15555/100

                Log.i(DEBUG_TAG, "lat: $latitude, lon: $longitude")
                Log.i(DEBUG_TAG, "trueLat: $trueLat, trueLong: $trueLon")

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

                markerList.add(Marker(map))

                markerList.last().position = GeoPoint(latitude, longitude)
                markerList.last().icon = ContextCompat.getDrawable(this, R.drawable.camera)
                markerList.last().setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                // TODO : Show image when marker is clicked.
                map.overlays.add(markerList.last())
                Log.i(DEBUG_TAG, "Image Marker Added to Map")

                storeHouseNumbersObject.addImage(currentImagePath!!, latitude, longitude)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else if (requestCode == 5 && resultCode == RESULT_OK) {

            Log.i(DEBUG_TAG, "attempting to store zip file.")

            storeHouseNumbersObject.writeToOsmFile()

            Log.i(DEBUG_TAG, "Addresses and Notes written to XML file in internal storage.")
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
                    zipFilesAndDelete(data?.data!!)
                    Log.i(DEBUG_TAG, "Data has been zipped and stored")
                    // clear database
                    storeHouseNumbersObject.clearDatabase()
                    Log.i(DEBUG_TAG, "Database cleared.")
                    // clear markers
                    for (marker: Marker in markerList) {
                        map.overlays.remove(marker)
                        map.invalidate()
                    }
                    Log.i(DEBUG_TAG, "Database cleared and markers have been removed.")
                    runOnUiThread {
                        dialog.dismiss()
                    }
                    Log.i(DEBUG_TAG, "Dialog dismissed")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this,
                            getString(R.string.failed_save), Toast.LENGTH_SHORT).show()
                    runOnUiThread {
                        dialog.dismiss()
                    }
                    Log.i(DEBUG_TAG, "Dialog dismissed")
                }
            }.start()
        }

    }

    private fun zipFilesAndDelete(zipUri : Uri) {
        val buffer = 2048

        val surveyFolder = File(getExternalFilesDir(null)!!.absolutePath)
        val surveyFiles: Array<File>? = surveyFolder.listFiles()

        if (surveyFiles != null) {
            val zipOutputStream = ZipOutputStream(
                    contentResolver.openOutputStream(zipUri)
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
            }
            zipOutputStream.close()
            Log.i(DEBUG_TAG, "zipFilesAndDelete: files zipped")
            for (file in surveyFiles) {
                file.delete()
            }
            Log.i(DEBUG_TAG, "zipFilesAndDelete: files deleted")

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
        when (imagery) {
            "mapnik_imagery" -> map.setTileSource(mapnik)
            //"mapbox_satellite" -> map.setTileSource(mapbox_satellite)
            "public_transport_map" -> map.setTileSource(publicTransportMap)
        }

    }

    // If undo button is pressed
    fun undo(view: View) {
        if (markerList.isNotEmpty() ) {
            if (storeHouseNumbersObject.lastItemType() == "Image") {
                Log.i(DEBUG_TAG, "Last Item was an image.")
                val deleteFileDialog = AlertDialog.Builder(this)

                deleteFileDialog.setPositiveButton(getString(R.string.delete)) { _, _ ->
                    storeHouseNumbersObject.undo(true)
                    map.overlays.remove(markerList.last())
                    map.invalidate()
                    markerList.removeLast()
                }

                deleteFileDialog.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
                deleteFileDialog.setTitle(getString(R.string.delete_image))
                deleteFileDialog.setMessage(getString(R.string.delete_image_question))
                deleteFileDialog.create().show()
            } else {
                storeHouseNumbersObject.undo(false)
                map.overlays.remove(markerList.last())
                map.invalidate()
                markerList.removeLast()
            }


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
        val vibrator: Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                    VibrationEffect.createOneShot(
                            80, VibrationEffect.DEFAULT_AMPLITUDE
                    )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(80)
        }

        val addNoteBuilder = AlertDialog.Builder(this)
        addNoteBuilder.setTitle(getString(R.string.add_note))

        var noteContents: String
        val note = EditText(this)
        note.minLines = 5
        val container = FrameLayout(this)
        val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
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

        val dialog = addNoteBuilder.create()
        dialog.show()
        note.requestFocus()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

    }


    // Save all the data collected to an .osm file, clear markers
    fun saveData(view: View) {

        val saveDataDialogue  = AlertDialog.Builder(this)
        saveDataDialogue.setTitle(getString(R.string.change_increment))
        saveDataDialogue.setMessage(getString(R.string.save_data_question))

        saveDataDialogue.setNeutralButton("Cancel") { _, _ -> }

        saveDataDialogue.setPositiveButton(getString(R.string.save)) { _, _ ->

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/zip"
            intent.putExtra(Intent.EXTRA_TITLE, "survey.zip")

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
        Log.i(DEBUG_TAG, "onLongPress")
        longPressDetected = true
        Log.i("in le test", "longpressdetected $longPressDetected")
    }

    override fun onFling(
            downEvent: MotionEvent,
            moveEvent: MotionEvent,
            velocityX: Float,
            velocityY: Float
    ): Boolean {
        Log.i(DEBUG_TAG, "onFling")
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
        // TODO : Mini menu shows up when you swipe up
        return true
    }


    fun showMiniKeypadPopupMenu(view: View) {
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
            else -> {
                return false
            }
        }

    }

    private fun modifyIncrement() {
        val incrementButtonDimensions = 200
        val textBoxWidth = 200
        val textBoxTextSize = 40f

        val sharedPreferences = getSharedPreferences(getString(R.string.preference_string), MODE_PRIVATE)
        increment = sharedPreferences.getInt("increment", 2)

        Log.i(DEBUG_TAG, "Increment before function: $increment")
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
                try {
                    var textToInt = text.toInt()
                    textToInt -= 1
                    modifyIncrementInput.setText(textToInt.toString())
                } catch (e: NumberFormatException) {
                    var textToSet = "";

                    for (c in text) {
                        val intOrNot = c.toString().toIntOrNull()
                        if (intOrNot != null) {
                            textToSet += intOrNot.toString()
                            Log.i(textToSet, "text to set")
                        }
                    }
                    textToSet = (textToSet.toInt() - 1).toString()
                    modifyIncrementInput.setText(textToSet)

                    Log.i(textToSet, "final text")
                }
            }
            Log.i(DEBUG_TAG, "modifyIncrementDialog: minusButton pressed")
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
                try {
                    var textToInt = text.toInt()
                    textToInt += 1
                    modifyIncrementInput.setText(textToInt.toString())
                } catch (e: NumberFormatException) {
                    var textToSet = "";

                    for (c in text) {
                        val intOrNot = c.toString().toIntOrNull()
                        if (intOrNot != null) {
                            textToSet += intOrNot.toString()
                            Log.i(textToSet, "text to set")
                        }
                    }
                    textToSet = (textToSet.toInt() + 1).toString()
                    modifyIncrementInput.setText(textToSet)

                    Log.i(textToSet, "final text")
                }
            }
            Log.i(DEBUG_TAG, "modifyIncrementDialog: plusButton pressed")
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
                Log.i(DEBUG_TAG, "incrementValue in Dialog: $incrementValue")
                increment = incrementValue.toInt()

                val sharedPreferencesEditor = getSharedPreferences(getString(R.string.preference_string), MODE_PRIVATE).edit()
                sharedPreferencesEditor.putInt("increment", increment)
                sharedPreferencesEditor.apply()
            } catch (e: TypeCastException) {
                e.printStackTrace()
                Log.e(DEBUG_TAG, getString(R.string.increment_not_integer))
                Toast.makeText(
                        this, getString(R.string.increment_not_integer),
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
        modifyIncrementDialogue.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
        modifyIncrementDialogue.create().show()
    }

    fun swipeAddHousenumber(view: View) {
        val buildingLevelsButton = findViewById<ImageButton>(view.id)
        val relativeLayout = buildingLevelsButton.parent as RelativeLayout
        val buildingLevelsTextView = relativeLayout.getChildAt(1) as TextView
        val houseNumber = findViewById<TextView>(R.id.mini_keypad_housenumber)

        val vibrator: Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        Thread {
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(
                        VibrationEffect.createOneShot(
                                80, VibrationEffect.DEFAULT_AMPLITUDE
                        )
                )
                Thread.sleep(100)
                vibrator.vibrate(
                        VibrationEffect.createOneShot(
                                80, VibrationEffect.DEFAULT_AMPLITUDE
                        )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(80)
                @Suppress("DEPRECATION")
                vibrator.vibrate(80)
            }
        }.start()

        Log.i(DEBUG_TAG, "buildingLevels: ${buildingLevelsTextView.text}")
        val addressToChange = storeHouseNumbersObject.lastAddressEntry(
                buildingLevelsButton.tag.toString()
        )

        addressToChange!!.buildingLevels = buildingLevelsTextView.text.toString()
        addressToChange.latitude = map.mapCenter.latitude
        addressToChange.longitude = map.mapCenter.longitude
        addressToChange.housenumber = houseNumber.text.toString()

        addHousenumberMarker(addressToChange)
        storeHouseNumbersObject.addHouseNumber(addressToChange)

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

        Log.i(DEBUG_TAG, "buildingLevels, ${addressToChange.buildingLevels}")
        buildingLevelsButton.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, android.R.color.holo_green_dark)
        )

        Log.i(DEBUG_TAG, "flingUpDetected & address is not null")

        val sharedPreferences = getSharedPreferences(getString(R.string.preference_string), MODE_PRIVATE)
        increment = sharedPreferences.getInt("increment", 2)

        try {
            var numToIncrement = houseNumber.text.toString().toInt()
            numToIncrement += increment
            houseNumber.text = numToIncrement.toString()
        } catch (e: Exception) {
            var textToSet = "";

            for (c in houseNumber.text) {
                val intOrNot = c.toString().toIntOrNull()
                if (intOrNot != null) {
                    textToSet += intOrNot.toString()
                    Log.i(textToSet, "text to set")
                }
            }
            textToSet = (textToSet.toInt() + increment).toString()
            houseNumber.text = textToSet

            Log.i("final text", textToSet)

        }
        map.invalidate()
    }

    fun decrementMiniHousenumber(view: View) {
        val houseNumber = findViewById<TextView>(R.id.mini_keypad_housenumber)
        try {
            var numToIncrement = houseNumber.text.toString().toInt()
            numToIncrement -= 1
            houseNumber.text = numToIncrement.toString()
        } catch (e: Exception) {
            var textToSet = "";

            for (c in houseNumber.text) {
                val intOrNot = c.toString().toIntOrNull()
                if (intOrNot != null) {
                    textToSet += intOrNot.toString()
                    Log.i(textToSet, "text to set")
                }
            }
            textToSet = (textToSet.toInt() - 1).toString()
            houseNumber.text = textToSet

            Log.i("final text", textToSet)
        }


    }

    fun incrementMiniHousenumber(view: View) {

        val houseNumber = findViewById<TextView>(R.id.mini_keypad_housenumber)
        try {
            var numToIncrement = houseNumber.text.toString().toInt()
            numToIncrement += 1
            houseNumber.text = numToIncrement.toString()
        } catch (e: Exception) {
            var textToSet = "";

            for (c in houseNumber.text) {
                val intOrNot = c.toString().toIntOrNull()
                if (intOrNot != null) {
                    textToSet += intOrNot.toString()
                    Log.i(textToSet, "text to set")
                }
            }
            textToSet = (textToSet.toInt() + 1).toString()
            houseNumber.text = textToSet

            Log.i("final text", textToSet)

        }
    }

    fun closeMiniKeypad(view: View) {
        slideUp.hide()
    }

    fun takePhoto(view: View) {

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

        if (isExternalStorageAvailable && !isExternalStorageReadOnly) {
            val current = SimpleDateFormat("dd-mm-yyyy-hh-mm-ss").format(Date())
            val fileName = "image-$current.jpg"
/*
            val folderPath = Environment.getExternalStorageDirectory().absolutePath +
                    File.separator + "SwiftAddress" + File.separator

            if (!File(folderPath).exists()) {
                File(folderPath).mkdir()
            }

 */
            val imageFile = File(getExternalFilesDir(null), fileName)
            currentImagePath = imageFile.absolutePath

            Log.i(DEBUG_TAG, "filePath: ${getExternalFilesDir(null)!!.absolutePath + fileName}")
            Log.i(DEBUG_TAG, "filePath: $currentImagePath")
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(packageManager) != null) {

                val imageURI : Uri = FileProvider.getUriForFile(this,
                    "com.mapitall.SwiftAddress.provider",
                    imageFile
                    )
                Log.i(DEBUG_TAG, "URI filePath: $imageURI")
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
                startActivityForResult(cameraIntent, 4)
                }
            }
        else {
            Toast.makeText(this, R.string.give_storage_permission,
                    Toast.LENGTH_SHORT).show()
        }
    }


}
