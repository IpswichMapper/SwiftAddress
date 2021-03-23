package com.mapitall.SwiftAddress

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import org.apache.commons.lang3.StringUtils
import java.lang.NullPointerException
import java.net.URL
import kotlin.math.abs
import kotlin.math.cos

class ClassicMainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private val storeHouseNumbersObject = StoreHouseNumbers(this)

    private val TAG = "ClassicMainActivity"

    private lateinit var gestureDetector: GestureDetectorCompat
    private var onFlingDetected = "no"
    private var touchEvent = false
    private var street = ""
    private var buildingLevels = ""
    private var increment  = 2
    private lateinit var gpsTracker : GPSTracker

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classic_main)
        supportActionBar?.hide()

        gpsTracker = GPSTracker(this)

        val navMenu = findViewById<NavigationView>(R.id.classic_nav_menu)

        gestureDetector = GestureDetectorCompat(this, this)

        val addressTextBox = findViewById<EditText>(R.id.classic_address_textbox)
        addressTextBox.requestFocus()

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

        val rightPlusButton = findViewById<ImageButton>(R.id.classic_add_right)
        rightPlusButton.setOnTouchListener { _, event ->
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            if (onFlingDetected == "up") {
                modifyOffset()
                touchEvent = true
            }

            return@setOnTouchListener super.onTouchEvent(event)
        }
        rightPlusButton.setOnClickListener {
            if (!touchEvent) {
                incrementRightAddress(it)
            }
        }


        val backspaceButton = findViewById<ImageButton>(R.id.classic_backspace)
        backspaceButton.setOnLongClickListener {
            addressTextBox.text.clear()
            return@setOnLongClickListener true
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
                    saveData(navMenu)
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

        val streetNameTag = findViewById<TextView>(R.id.classic_street_name_tag)
        val streetNameValue = findViewById<TextView>(R.id.classic_street_name_value)
        streetNameTag.setOnClickListener {
            modStreetName()
        }
        streetNameValue.setOnClickListener {
            modStreetName()
        }
    }

    private fun modBuildLevels() {
        Toast.makeText(this, getString(R.string.unimplemented), Toast.LENGTH_SHORT).show()
    }

    // Modify the distance that a housenumber is offset from your current location.
    private fun modifyOffset() {
        val offsetButtonsDimensions = 200
        val textBoxWidth = 200
        val textBoxTextSize = 40f
        val modifyOffsetAlertDialog = AlertDialog.Builder(this)
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        var offset = sp.getInt("offset", 10)

        offset = sp.getInt("offset", 2)

        Log.i(TAG, "offset before function: $offset")
        val modifyoffsetDialogue  = AlertDialog.Builder(this)
        modifyoffsetDialogue.setTitle(getString(R.string.change_offset))

        var offsetValue : String

        val modifyoffsetInput = EditText(this)
        modifyoffsetInput.inputType = EditorInfo.TYPE_CLASS_PHONE
        modifyoffsetInput.setText(offset.toString())
        modifyoffsetInput.textSize = textBoxTextSize

        modifyoffsetInput.layoutParams = ViewGroup.LayoutParams(textBoxWidth, MATCH_PARENT)
        modifyoffsetInput.gravity =CENTER

        val minusButton = ImageButton(this)
        minusButton.setImageResource(R.drawable.minus)

        minusButton.layoutParams = ViewGroup.LayoutParams(offsetButtonsDimensions,
                offsetButtonsDimensions)
        minusButton.setOnClickListener {
            val text = modifyoffsetInput.text.toString()
            if (text != "") {
                try {
                    var textToInt = text.toInt()
                    textToInt -= 1
                    modifyoffsetInput.setText(textToInt.toString())
                } catch (e: NumberFormatException) {
                    var textToSet = ""

                    for (c in text) {
                        val intOrNot = c.toString().toIntOrNull()
                        if (intOrNot != null) {
                            textToSet += intOrNot.toString()
                            Log.i(textToSet, "text to set")
                        }
                    }
                    textToSet = (textToSet.toInt() - 1).toString()
                    modifyoffsetInput.setText(textToSet)

                    Log.i(textToSet, "final text")
                }
            }
            Log.i(TAG, "modifyoffsetDialog: minusButton pressed")
        }

        val plusButton = ImageButton(this)
        plusButton.setImageResource(R.drawable.plus)
        plusButton.layoutParams = ViewGroup.LayoutParams(offsetButtonsDimensions,
                offsetButtonsDimensions)
        plusButton.setOnClickListener {
            val text = modifyoffsetInput.text.toString()
            if (text != "") {
                try {
                    var textToInt = text.toInt()
                    textToInt += 1
                    modifyoffsetInput.setText(textToInt.toString())
                } catch (e: NumberFormatException) {
                    var textToSet = ""

                    for (c in text) {
                        val intOrNot = c.toString().toIntOrNull()
                        if (intOrNot != null) {
                            textToSet += intOrNot.toString()
                            Log.i(textToSet, "text to set")
                        }
                    }
                    textToSet = (textToSet.toInt() + 1).toString()
                    modifyoffsetInput.setText(textToSet)

                    Log.i(textToSet, "final text")
                }
            }
            Log.i(TAG, "modifyOffsetDialog: plusButton pressed")
        }

        val linearLayout = LinearLayout(this)

        linearLayout.layoutParams = ActionBar.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.gravity = CENTER
        linearLayout.addView(minusButton)
        linearLayout.addView(modifyoffsetInput)
        linearLayout.addView(plusButton)

        modifyoffsetDialogue.setView(linearLayout)
        modifyoffsetDialogue.setMessage(getString(R.string.offset_message))

        modifyoffsetDialogue.setPositiveButton(getString(R.string.save_offset)) {
            _, _ ->
            offsetValue = modifyoffsetInput.text.toString()
            try {
                Log.i(TAG, "offsetValue in Dialog: $offsetValue")
                offset = offsetValue.toInt()

                val sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(
                        this).edit()
                sharedPreferencesEditor.putInt("offset", offset)
                sharedPreferencesEditor.apply()
            } catch (e: TypeCastException) {
                e.printStackTrace()
                Log.e(TAG, getString(R.string.offset_not_integer))
                Toast.makeText(this, getString(R.string.offset_not_integer),
                        Toast.LENGTH_SHORT).show()
            }
        }
        modifyoffsetDialogue.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
        modifyoffsetDialogue.create().show()
    }

    private fun modStreetName() {
        val changeStreetDialogue  = AlertDialog.Builder(this)
        changeStreetDialogue.setTitle(getString(R.string.street_name))

        var streetNameValue : String
        val streetNameInput = AutoCompleteTextView(this)

        val lat = intent.getDoubleExtra("lat", 0.000)
        val lon = intent.getDoubleExtra("lon", 0.000)
        val radius = 100

        Thread {
            Log.i(TAG, "In thread")

            val queryText = "https://overpass-api.de/api/interpreter?data=" +
                    "<query type='way'><around lat='$lat' lon='$lon' radius='$radius'/>" +
                    "<has-kv k='highway' regv='trunk|primary|secondary|tertiary|unclassified" +
                    "|residential|living_street|pedestrian|road' />" +
                    "<has-kv k='name' regv='.+'></has-kv></query><print/>"

            val query = URL(queryText)
            val result = query.readText()
            try {
                var array: Array<String> = StringUtils.substringsBetween(result,
                        "<tag k=\"name\" v=\"", "\"/>")
                val distinctList = array.distinct()
                Log.e(TAG, distinctList.toString())

                runOnUiThread {
                    Log.i(TAG, "in runOnUiThread")
                    streetNameInput.setAdapter(ArrayAdapter(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            distinctList))
                    Log.i(TAG, "Query finished")
                    Toast.makeText(this, "Query finished", Toast.LENGTH_SHORT).show()

                }
            } catch (e : NullPointerException) {
                Log.i(TAG, "Failed to find any street names.")
                Log.w(TAG, result)
            }

        }.start()

        val container = FrameLayout(this)
        val params : FrameLayout.LayoutParams = FrameLayout.LayoutParams(
                MATCH_PARENT, WRAP_CONTENT
        )
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        streetNameInput.layoutParams = params

        container.addView(streetNameInput)

        changeStreetDialogue.setView(container)
        changeStreetDialogue.setMessage(getString(R.string.remember_change_street_name))

        changeStreetDialogue.setPositiveButton(getString(R.string.change_street_name_button)) {
            _, _ -> streetNameValue = streetNameInput.text.toString()

            val streetNameTextView = findViewById<TextView>(R.id.classic_street_name_value)

            if (streetNameValue.length < 18) {
                streetNameTextView.text = streetNameValue
            } else {
                streetNameTextView.text = "${streetNameValue.subSequence(0, 15)}..."
            }

            street = streetNameValue
        }
        changeStreetDialogue.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
        val dialog = changeStreetDialogue.create()
        dialog.show()
        streetNameInput.requestFocus()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

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

        textbox.append(numButton.text)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult")

        // Saves data
        if (requestCode == 5 && resultCode == RESULT_OK) {

            Log.i(TAG, "attempting to store zip file.")

            storeHouseNumbersObject.writeToOsmFile()

            Log.i(TAG, "Addresses and Notes written to XML file in internal storage.")
            val savingFilesDialog = AlertDialog.Builder(this)
            savingFilesDialog.setTitle(getString(R.string.saving_files))
            savingFilesDialog.setMessage(getString(R.string.please_wait))
            savingFilesDialog.setCancelable(false)
            val dialog = savingFilesDialog.create()

            Thread {

                try {
                    runOnUiThread {
                        dialog.show()
                    }
                    // create zip file, delete app internal storage, store zip file to chosen location.
                    storeHouseNumbersObject.zipFilesAndDelete(data?.data!!)
                    Log.i(TAG, "Data has been zipped and stored")
                    // clear database
                    storeHouseNumbersObject.clearDatabase()
                    Log.i(TAG, "Database cleared.")

                    runOnUiThread {
                        dialog.dismiss()
                        Log.i(TAG, "Dialog dismissed.")
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

        if (requestCode == 7 && resultCode == RESULT_OK) {
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

            val location = gpsTracker.getLocation()!!
            val lat = location.latitude
            val lon = location.longitude

            try {
                var azimuth = gpsTracker.getAzimuth()!!.toDouble()
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
                var latOffset = cos(angle) * length

                // Length times cos of (angle - 90 degrees) gives offset in East
                // If bearing is nearer to west, it will give negative value
                // (Math.PI / 2) is 90 degrees in radians.
                var lonOffset = cos(angle - (Math.PI / 2)) * length

                // Dividing offsets by 111.111km to turn them from metres to LatLon co-ordinates
                // TODO: Make this more accurate.
                //  Number to divide by changes at different locations around the world.
                latOffset /= 111111

                Log.i(TAG, "lat: $lat, lon: $lon")
                if (abs(lat) < 23) {
                    lonOffset /= 111320
                    Log.i(TAG, "latOffset when absolute of longitude is less than 23")
                }
                else if (abs(lat) >= 23 && abs(lat) < 45) {
                    lonOffset /= 102470
                    Log.i(TAG, "latOffset when absolute of longitude is less than 45")
                }
                else if (abs(lat) >= 45 && abs(lat) < 67) {
                    lonOffset /= 78710
                    Log.i(TAG, "latOffset when absolute of longitude is less than 67")
                }
                else {
                    lonOffset /= 43496
                    Log.i(TAG, "latOffset when absolute of longitude is more than 67")
                }
                val trueLat = lat + latOffset
                val trueLon = lon + lonOffset

                val houseNumber = findViewById<EditText>(R.id.classic_address_textbox)
                    .text.toString()
                val buildingLevels = findViewById<TextView>(R.id.classic_building_levels_value)
                    .text.toString()
                val sideString : String =
                    if(side == Side.LEFT) "left"
                    else if(side == Side.RIGHT) "right"
                    else "forward"
                val address = AddressNodes(houseNumber,
                    street,
                    trueLat,
                    trueLon,
                    sideString,
                    buildingLevels)
                storeHouseNumbersObject.addHouseNumber(address)

                Log.i(TAG, "Housenumber Added")
                Log.i(TAG, "azimuth : $azimuth")
                Log.i(TAG, "trueLat : $trueLat")
                Log.i(TAG, "trueLon : $trueLon")
                Log.i(TAG, "sideString : $sideString")

                findViewById<EditText>(R.id.classic_address_textbox).text.clear()


            } catch (e : NullPointerException) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.compass_unavailable),
                    Toast.LENGTH_SHORT).show()
            }

        } catch (e: NullPointerException) {
            e.printStackTrace()
            Toast.makeText(this, R.string.location_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    // These two functions increment or decrement the from the previous address that was added
    // on the left. It increments / decrements textbox if the textbox contains anything
    fun incrementLeftAddress(view: View) {
        val address = storeHouseNumbersObject.lastAddressEntry("left")
        if (address != null) {

            val addressTextbox = findViewById<EditText>(R.id.classic_address_textbox)
            val addressTextboxText = addressTextbox.text.toString()
            var addressTextboxHint: String
            try {
                addressTextboxHint = addressTextbox.hint.toString()
            } catch (e : NullPointerException) {
                addressTextboxHint = ""
            }
            if (addressTextboxText != "") {
                try {
                    var numToIncrement = addressTextboxText.toInt()
                    numToIncrement += 1
                    addressTextbox.text.clear()
                    addressTextbox.append(numToIncrement.toString())
                } catch (e: NumberFormatException) {

                    var textToSet = ""
                    if (addressTextboxText.isNotBlank()) {
                        for (c in addressTextboxText) {
                            val intOrNot = c.toString().toIntOrNull()
                            if (intOrNot != null) {
                                textToSet += intOrNot.toString()
                                Log.i(textToSet, "text to set")
                            }
                        }
                        textToSet = (textToSet.toInt() + 1).toString()
                        addressTextbox.text.clear()
                        addressTextbox.append(textToSet)
                    }
                    Log.i(textToSet, "final text")
                }
            } else  {
                try {
                    var numToIncrement = address.housenumber.toInt()
                    numToIncrement += 1
                    addressTextbox.text.clear()
                    addressTextbox.append(numToIncrement.toString())
                } catch (e: NumberFormatException) {
                    var textToSet = ""

                    for (c in address.housenumber) {
                        val intOrNot = c.toString().toIntOrNull()
                        if (intOrNot != null) {
                            textToSet += intOrNot.toString()
                            Log.i(textToSet, "text to set")
                        }
                    }
                    textToSet = (textToSet.toInt() + 1).toString()
                    addressTextbox.text.clear()
                    addressTextbox.append(textToSet)

                    Log.i(textToSet, "final text")
                }
            }
        }
    }

    fun decrementLeftAddress(view: View) {

        val address = storeHouseNumbersObject.lastAddressEntry("left")

        if (address != null) {
            val addressTextbox = findViewById<EditText>(R.id.classic_address_textbox)
            val addressTextboxText = addressTextbox.text.toString()

            if (addressTextboxText != "") {
                try {
                    var num_to_decrement = addressTextboxText.toInt()
                    num_to_decrement -= 1
                    addressTextbox.text.clear()
                    addressTextbox.append(num_to_decrement.toString())
                } catch (e: NumberFormatException) {
                    var textToSet = ""
                    if (addressTextboxText.isNotBlank()) {
                        for (c in addressTextboxText) {
                            val intOrNot = c.toString().toIntOrNull()
                            if (intOrNot != null) {
                                textToSet += intOrNot.toString()
                                Log.i(textToSet, "text to set")
                            }
                        }
                        textToSet = (textToSet.toInt() - 1).toString()
                        addressTextbox.text.clear()
                        addressTextbox.append(textToSet)
                    }
                    Log.i(textToSet, "final text")
                }
            } else if (address.housenumber != "") {
                try {
                    var numToDecrement = address.housenumber.toInt()
                    numToDecrement -= 1
                    addressTextbox.text.clear()
                    addressTextbox.append(numToDecrement.toString())
                } catch (e: NumberFormatException) {
                    var textToSet = ""

                    for (c in address.housenumber) {
                        val intOrNot = c.toString().toIntOrNull()
                        if (intOrNot != null) {
                            textToSet += intOrNot.toString()
                            Log.i(textToSet, "text to set")
                        }
                    }
                    textToSet = (textToSet.toInt() - 1).toString()
                    addressTextbox.text.clear()
                    addressTextbox.append(textToSet)

                    Log.i(textToSet, "final text")
                }
            }
        }
    }

    // These two functions increment or decrement the from the previous address that was added
    // on the right. It increments / decrements textbox if the textbox contains anything
    fun incrementRightAddress(view: View) {

        val address = storeHouseNumbersObject.lastAddressEntry("right")

        if (address != null) {
            val addressTextbox = findViewById<EditText>(R.id.classic_address_textbox)
            val addressTextboxText = addressTextbox.text.toString()

            if (addressTextboxText != "") {
                try {
                    var numToIncrement = addressTextboxText.toInt()
                    numToIncrement += 1
                    addressTextbox.text.clear()
                    addressTextbox.append(numToIncrement.toString())
                } catch (e: NumberFormatException) {
                    var textToSet = ""
                    if (addressTextboxText.isNotBlank()) {
                        for (c in addressTextboxText) {
                            val intOrNot = c.toString().toIntOrNull()
                            if (intOrNot != null) {
                                textToSet += intOrNot.toString()
                                Log.i(textToSet, "text to set")
                            }
                        }
                        textToSet = (textToSet.toInt() + 1).toString()
                        addressTextbox.text.clear()
                        addressTextbox.append(textToSet)
                    }
                    Log.i(textToSet, "final text")
                }
            } else if (address.housenumber != "") {
                try {
                    var numToIncrement = address.housenumber.toInt()
                    numToIncrement += 1
                    addressTextbox.text.clear()
                    addressTextbox.append(numToIncrement.toString())
                } catch (e: NumberFormatException) {
                    var textToSet = ""

                    for (c in address.housenumber) {
                        val intOrNot = c.toString().toIntOrNull()
                        if (intOrNot != null) {
                            textToSet += intOrNot.toString()
                            Log.i(textToSet, "text to set")
                        }
                    }
                    textToSet = (textToSet.toInt() + 1).toString()
                    addressTextbox.text.clear()
                    addressTextbox.append(textToSet)

                    Log.i(textToSet, "final text")
                }
            }
        }
    }

    fun decrementRightAddress(view: View) {

        val address = storeHouseNumbersObject.lastAddressEntry("right")

        if (address != null) {
            val addressTextbox = findViewById<EditText>(R.id.classic_address_textbox)
            val addressTextboxText = addressTextbox.text.toString()

            if (addressTextboxText != "") {
                try {
                    var num_to_decrement = addressTextboxText.toInt()
                    num_to_decrement -= 1
                    addressTextbox.text.clear()
                    addressTextbox.append(num_to_decrement.toString())
                } catch (e: NumberFormatException) {
                    var textToSet = ""
                    if (addressTextboxText.isNotBlank()) {
                        for (c in addressTextboxText) {
                            val intOrNot = c.toString().toIntOrNull()
                            if (intOrNot != null) {
                                textToSet += intOrNot.toString()
                                Log.i(textToSet, "text to set")
                            }
                        }
                        textToSet = (textToSet.toInt() - 1).toString()
                        addressTextbox.text.clear()
                        addressTextbox.append(textToSet)
                    }
                    Log.i(textToSet, "final text")
                }
            } else if (address.housenumber != "") {
                try {
                    var numToDecrement = address.housenumber.toInt()
                    numToDecrement -= 1
                    addressTextbox.text.clear()
                    addressTextbox.append(numToDecrement.toString())
                } catch (e: NumberFormatException) {
                    var textToSet = ""

                    for (c in address.housenumber) {
                        val intOrNot = c.toString().toIntOrNull()
                        if (intOrNot != null) {
                            textToSet += intOrNot.toString()
                            Log.i(textToSet, "text to set")
                        }
                    }
                    textToSet = (textToSet.toInt() - 1).toString()
                    addressTextbox.text.clear()
                    addressTextbox.append(textToSet)

                    Log.i(textToSet, "final text")
                }
            }
        }
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

        val saveDataDialogue  = AlertDialog.Builder(this)
        saveDataDialogue.setTitle(getString(R.string.save_data))
        saveDataDialogue.setMessage(getString(R.string.save_data_question))

        saveDataDialogue.setNeutralButton("Cancel") { _, _ -> }

        saveDataDialogue.setPositiveButton(getString(R.string.save)) { _, _ ->

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/zip"
            intent.putExtra(Intent.EXTRA_TITLE, "survey.zip")

            startActivityForResult(intent, 1)
        }

        saveDataDialogue.create().show()
        // storeHouseNumbersObject.writeToOsmFile()

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