package com.mapitall.SwiftAddress

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import kotlin.math.abs
import kotlin.math.cos

class ClassicMainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private val storeHousenumbersObject = StoreHouseNumbers(this)
    private val sensorTracker = GPSTracker(this)

    private val TAG = "ClassicMainActivity"

    private lateinit var gestureDetector: GestureDetectorCompat
    private var onFlingDetected = "no"
    private var touchEvent = false
    private var street = ""
    private var buildingLevels = ""
    private var increment  = 2

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classic_main)
        supportActionBar?.hide()

        val navMenu = findViewById<NavigationView>(R.id.classic_nav_menu)

        gestureDetector = GestureDetectorCompat(this, this)

        // Onclick listeners and ontouch listeners for the keypad buttons
        // "Ontouch" is activated when you do any action on the buttons, for example touch or swipe
        // After "ontouch", the code checks if you swiped your finger up or down
        // and then if you did, the app executes the action for a swipe up or down.
        // If you swiped up or down, the onclicklistener does nothing.
        // Otherwise, if you tapped on the button, the onclick listener adds the number
        // to the textbox.
        val numButton1 = findViewById<Button>(R.id.classic_keypad_num1)
        numButton1.setOnTouchListener { _, event ->
            Log.i(TAG, "numButton1: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = findViewById<TextView>(R.id.classic_lettera).text.toString()
            val swipeDownText = ""

            if (onFlingDetected != "no") {
                addNum(numButton1, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton1.setOnClickListener {
            Log.i(TAG, "numButton1: onTouchListener Called")
            if (!touchEvent) {
                Log.i(TAG, "numButton2: clicked.")
                Log.i(TAG, "numButton2: addNum(numButton4) called")
                addNum(numButton1)
            } else {
                touchEvent = false
            }
        }

        val numButton2 = findViewById<Button>(R.id.classic_keypad_num2)
        numButton2.setOnTouchListener { _, event ->
            Log.i(TAG, "numButton2: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = findViewById<TextView>(R.id.classic_letterb).text.toString()
            val swipeDownText = ""

            if (onFlingDetected != "no") {
                addNum(numButton2, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton2.setOnClickListener {
            Log.i(TAG, "numButton2: onTouchListener Called")
            if (!touchEvent) {
                Log.i(TAG, "numButton2: clicked.")
                Log.i(TAG, "numButton2: addNum(numButton4) called")
                addNum(numButton2)
            } else {
                touchEvent = false
            }
        }

        val numButton3 = findViewById<Button>(R.id.classic_keypad_num3)
        numButton3.setOnTouchListener { _, event ->
            Log.i(TAG, "numButton3: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = findViewById<TextView>(R.id.classic_letterc).text.toString()
            val swipeDownText = ""

            if (onFlingDetected != "no") {
                addNum(numButton3, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton3.setOnClickListener {
            Log.i(TAG, "numButton3: onTouchListener Called")
            if (!touchEvent) {
                Log.i(TAG, "numButton3: clicked.")
                Log.i(TAG, "numButton3: addNum(numButton4) called")
                addNum(numButton3)
            }
        }

        val numButton4 = findViewById<Button>(R.id.classic_keypad_num4)
        numButton4.setOnTouchListener { _, event ->
            Log.i(TAG, "numButton4: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = findViewById<TextView>(R.id.classic_comma).text.toString()
            val swipeDownText = findViewById<TextView>(R.id.classic_B1R0).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton4, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton4.setOnClickListener {
            Log.i(TAG, "numButton4: onTouchListener Called")
            if (!touchEvent) {
                Log.i(TAG, "numButton4: clicked.")
                Log.i(TAG, "numButton4: addNum(numButton4) called")
                addNum(numButton4)
            }
        }


        val numButton5 = findViewById<Button>(R.id.classic_keypad_num5)
        numButton5.setOnTouchListener { _, event ->
            Log.i(TAG, "numButton5: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = "-"
            val swipeDownText = findViewById<TextView>(R.id.classic_B2R0).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton5, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton5.setOnClickListener {
            Log.i(TAG, "numButton5: onTouchListener Called")
            if (!touchEvent) {
                Log.i(TAG, "numButton5: clicked.")
                Log.i(TAG, "numButton5: addNum(numButton5) called")
                addNum(numButton5)
            }
        }

        val numButton6 = findViewById<Button>(R.id.classic_keypad_num6)
        numButton6.setOnTouchListener { _, event ->
            Log.i(TAG, "numButton6: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)

            val swipeUpText = findViewById<TextView>(R.id.classic_semicolon).text.toString()
            val swipeDownText = findViewById<TextView>(R.id.classic_B3R0).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton6, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton6.setOnClickListener {
            Log.i(TAG, "numButton6: onTouchListener Called")
            if (!touchEvent) {
                Log.i(TAG, "numButton6: clicked.")
                Log.i(TAG, "numButton6: addNum(numButton6) called")
                addNum(numButton6)
            }
        }

        val numButton7 = findViewById<Button>(R.id.classic_keypad_num7)
        numButton7.setOnTouchListener { _, event ->
            Log.i(TAG, "numButton7: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = ""
            val swipeDownText = findViewById<TextView>(R.id.classic_B1R1).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton7, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton7.setOnClickListener {
            Log.i(TAG, "numButton7: onTouchListener Called")
            if (!touchEvent) {
                Log.i(TAG, "numButton7: clicked.")
                Log.i(TAG, "numButton7: addNum(numButton7) called")
                addNum(numButton7)
          }
        }


        val numButton8 = findViewById<Button>(R.id.classic_keypad_num8)
        numButton8.setOnTouchListener { _, event ->
            Log.i(TAG, "numButton8: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = ""
            val swipeDownText = findViewById<TextView>(R.id.classic_B2R1).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton8, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton8.setOnClickListener {
            Log.i(TAG, "numButton8: onTouchListener Called")
            if (!touchEvent) {
                Log.i(TAG, "numButton8: clicked.")
                Log.i(TAG, "numButton8: addNum(numButton8) called")
                Log.i("keypad button ${numButton8.text}", "Clicked")
                addNum(numButton8)
            }
        }


        val numButton9 = findViewById<Button>(R.id.classic_keypad_num9)
        numButton9.setOnTouchListener { _, event ->
            Log.i(TAG, "numButton9: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = ""
            val swipeDownText = findViewById<TextView>(R.id.classic_B3R1).text.toString()

            Log.i("le out test", "onFlingDetected: $onFlingDetected")
            if (onFlingDetected != "no") {
                addNum(numButton9, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton9.setOnClickListener {
            Log.i(TAG, "numButton9: onClickListener Called")
          if (!touchEvent) {
              Log.i(TAG, "numButton9: clicked.")
              Log.i(TAG, "numButton9: addNum(numButton9) called")
              addNum(numButton9)
          }
        }


        val numButton0 = findViewById<Button>(R.id.classic_keypad_num0)
        numButton0.setOnTouchListener { _, event ->
            touchEvent = false
            onFlingDetected = "no"
            Log.w("key0", "ontouchactivated")
            gestureDetector.onTouchEvent(event)

            if (onFlingDetected == "up") {
                modStreetName()
                touchEvent = true
                Log.i(TAG, "numButton0: onFlingDetected = up, touchevent = true")
            } else if (onFlingDetected == "down") {
                modBuildLevels()
                touchEvent = true
                Log.i(TAG, "numButton0: onFlingDetected = up, touchevent = true")
            } else {
                touchEvent = false
                Log.w(TAG, "numButton0: onFlingDetected != up || down, touchEvent = false")
            }

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton0.setOnClickListener {
            Log.i("key0", "onclicklistener called.")
            if (!touchEvent) {
                addNum(numButton0)
            }
        }


        val backspaceButton = findViewById<ImageButton>(R.id.classic_backspace)
        backspaceButton.setOnTouchListener { _, event ->
            Log.i(TAG, "backspaceButton pressed")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)

            if (onFlingDetected == "up"){
                modifyIncrement()
            }

            return@setOnTouchListener super.onTouchEvent(event)
        }
        
        navMenu.setNavigationItemSelectedListener { menuItem ->
            val drawerLayout = findViewById<DrawerLayout>(R.id.classic_drawer_layout)
            val itemID = menuItem.itemId
            when(itemID) {
                R.id.recover_data_menu_option -> {
                    Toast.makeText(this, getString(R.string.unimplemented),
                            Toast.LENGTH_SHORT).show()
                }
                R.id.save_data_menu_option -> {
                    Toast.makeText(this, getString(R.string.unimplemented),
                            Toast.LENGTH_SHORT).show()
                }
                R.id.upload_data_menu_option -> {
                    Toast.makeText(this, getString(R.string.unimplemented),
                            Toast.LENGTH_SHORT).show()
                }
                R.id.settings_menu_option -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, Settings::class.java)
                    startActivityForResult(intent, 7)

                }
            }

            return@setNavigationItemSelectedListener true
        }

    }

    private fun modBuildLevels() {
        Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()
    }

    private fun modifyIncrement() {
        Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()
    }

    private fun modStreetName() {
        Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()
    }

    private fun addNum(numButton : Button, swipeUpText : String, swipeDownText : String) {
        val textbox = findViewById<EditText>(R.id.classic_address_textbox)


        if (onFlingDetected == "up") {
            if (swipeUpText != "") {
                // textbox.setText("${textbox.text}${swipeUpText}")
                textbox.append(swipeUpText)
                Log.i("addNum()", "$swipeUpText set")
            } else {
                Log.w("onFling", "There is no action for \"swipe up\"")
            }
        } else if (onFlingDetected == "down") {
            Log.w("swipeDownText:", swipeDownText)
            if (swipeDownText == "") {
                // TODO : Implement
                Log.w("onFling", "There is no action for \"swipe down\"")
            } else {
                val buildingLevelsValue = findViewById<TextView>(R.id.classic_building_levels_value)
                buildingLevelsValue.text = swipeDownText
                when (swipeDownText) {
                    "B1 R0" -> buildingLevelsValue.setTextColor(
                        ContextCompat.getColor(this, R.color.B1_R0))
                    "B2 R0" -> buildingLevelsValue.setTextColor(
                        ContextCompat.getColor(this, R.color.B2_R0))
                    "B3 R0" -> buildingLevelsValue.setTextColor(
                        ContextCompat.getColor(this, R.color.B3_R0))
                    "B1 R1" -> buildingLevelsValue.setTextColor(
                        ContextCompat.getColor(this, R.color.B1_R1))
                    "B2 R1" -> buildingLevelsValue.setTextColor(
                        ContextCompat.getColor(this, R.color.B2_R1))
                    "B3 R1" -> buildingLevelsValue.setTextColor(
                        ContextCompat.getColor(this, R.color.B3_R1))
                }
            }
        } else {
            Log.e("addNum()", "failed, onFlingDetected=$onFlingDetected")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addNum(numButton: Button) {
        val textbox = findViewById<EditText>(R.id.classic_address_textbox)

        textbox.text.clear()
        textbox.append("${textbox.text}${numButton.text}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult")

        if (requestCode == 7 && resultCode == RESULT_OK) {
            Log.i(TAG, "requestCode == 7 && resultCode == RESULT_OK")
            val sp = PreferenceManager.getDefaultSharedPreferences(this)

            if (sp.getBoolean("screen_timeout", false)) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            if (sp.getString("interface", "Classic") != "Classic") {
                val intent = Intent(this, MainActivity::class.java)

                startActivity(intent)
                finish()
            }
        }
    }

    // adds an address to database
    fun addAddress(view: View) {
        when(view.id) {
            R.id.classic_left_arrow_button -> addHouseNumber(Side.LEFT)

            R.id.classic_forward_arrow_button -> addHouseNumber(Side.FORWARD)

            R.id.classic_right_arrow_button -> addHouseNumber(Side.RIGHT)
        }
    }

    // TODO : FINISH
    // add housenumber to database.
    @SuppressLint("MissingPermission")
    private fun addHouseNumber(side: Side) {

        val length = 10 // housenumber will be placed with 10 metres offset from current location

        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria, false)!!
            val location = locationManager.getLastKnownLocation(provider)!!
            val oldLat = location.latitude
            val oldLon = location.longitude

            try {
                var azimuth = sensorTracker.azimuth!!.toDouble()
                if (azimuth < 0) azimuth += (2 * Math.PI)
                val bearing = Math.toRadians(azimuth)
                val angle : Double
                when (side) {
                    Side.LEFT -> angle = bearing - (Math.PI / 2)
                    Side.FORWARD -> angle = bearing
                    Side.RIGHT -> angle = bearing + (Math.PI / 2)
                }

                // Vector Mathematics

                // Length times cos of angle gives offset in North (will give negative value if
                // bearing is nearer to south
                val trueLon = cos(angle) * length

                // Length times cos of (angle - 90 degrees) gives offset in East
                // If bearing is nearer to west, it will give negative value
                // (Math.PI / 2) is 90 degrees in radians.
                val trueLat = cos(angle - (Math.PI / 2)) * length

                val houseNumber = findViewById<EditText>(R.id.classic_address_textbox)
                    .text.toString()
                val address = AddressNodes(houseNumber, "", trueLat, trueLon, "left", "")
                storeHousenumbersObject.addHouseNumber(address)



            } catch (e : Exception) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.compass_unavailable),
                    Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.location_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    // These two functions increment or decrement the from the previous address that was added
    // on the left. It increments / decrements textbox if the textbox contains anything
    fun incrementLeftAddress(view: View) {
        Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()
    }

    fun decrementLeftAddress(view: View) {
        Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()
    }

    // These two functions increment or decrement the from the previous address that was added
    // on the right. It increments / decrements textbox if the textbox contains anything
    fun incrementRightAddress(view: View) {
        Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()
    }

    fun decrementRightAddress(view: View) {
        Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()
    }


    // Removes last character from textbox (after pressing "backspace")
    fun textboxRemoveLastChar(view: View) {

        val addressTextbox = findViewById<EditText>(R.id.classic_address_textbox)
        var string = addressTextbox.text.toString()

        string = string.dropLast(1)
        addressTextbox.text.clear()
        addressTextbox.append(string)
    }

    fun openDrawer(view: View) {
        val drawerLayout = findViewById<DrawerLayout>(R.id.classic_drawer_layout)
        drawerLayout.openDrawer(GravityCompat.START)
    }
    fun saveData(view: View) {
        Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()
    }
    fun takePhoto(view: View) {
        Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()
    }
    fun undo(view: View) {
        Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()
    }



    // GestureDetector Methods
    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent?) {

    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {

    }

    // Detects if a swipe action on a keypad button was done, and checks if it was a
    // swipe up or swipe down action so that nothing happens when you swipe sideways.
    override fun onFling(downEvent: MotionEvent, moveEvent: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        Log.i(TAG, "onFlingDetected")

        val swipeValue = 100

        val differenceInY = moveEvent.y - downEvent.y
        val differenceInX = moveEvent.x - downEvent.x

        if (abs(differenceInY) > abs(differenceInX)) {
            // This is an up or down swipe

            if(abs(differenceInY) > swipeValue) {
                if (differenceInY < 0) {
                    // up swipe
                    Log.i("onFlingDetected", "up swipe")
                    this.onFlingDetected = "up"
                    Log.i("le in test", "onFlingDetected : $onFlingDetected")
                } else {
                    // down swipe
                    Log.i("onFlingDetected", "down swipe")
                    this.onFlingDetected = "down"
                }
            }
        }

        return true


    }





}