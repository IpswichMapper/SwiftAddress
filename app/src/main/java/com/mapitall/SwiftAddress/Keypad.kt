package com.mapitall.SwiftAddress

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.Gravity.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import layout.AddressNodes
import org.apache.commons.lang3.StringUtils
import java.net.URL

class Keypad : AppCompatActivity(), View.OnTouchListener,
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private lateinit var gestureDetector: GestureDetectorCompat
    private var onFlingDetected = "no"
    private var touchEvent = false
    private var street = ""
    private var buildingLevels = ""
    private var increment  = 2

    private val DEBUG_TAG = "Keypad"

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keypad)

        supportActionBar?.hide()

        // clear address textbox when backspace is long pressed
        findViewById<ImageButton>(R.id.backspace).setOnLongClickListener(View.OnLongClickListener {
            clearTextbox()
            return@OnLongClickListener true
        })

        var lastAddress = intent.getParcelableExtra<AddressNodes?>("last_address")

        gestureDetector = GestureDetectorCompat(this, this)

        val texbox = findViewById<EditText>(R.id.address_textbox)

        // Set hint to be housenumber of last address
        // Set street to be the street of last andress

        if (lastAddress != null) {
            texbox.hint = lastAddress.housenumber
        }


        val netInfo = (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)
                .activeNetworkInfo

        if (netInfo != null && netInfo.isConnected) {
            reverseGeocodeStreet(intent.getDoubleExtra("lat", 0.000), intent.getDoubleExtra("lon", 0.000))
        } else {
            Log.i(DEBUG_TAG, "No internet connection available. Street name " +
                    "now equals lastAddress street name.")
            if (lastAddress != null) {
                street = lastAddress.street

                val streetNameTextView = findViewById<TextView>(R.id.street_name_value)
                if (street.length < 18) {
                    streetNameTextView.text = street
                } else {
                    streetNameTextView.text = "${street.subSequence(0, 15)}..."
                }
            }
        }

        Log.i("street outside", street)

        // Onclick listeners and ontouch listeners for the keypad buttons
        // "Ontouch" is activated when you do any action on the buttons, for example touch or swipe
        // After "ontouch", the code checks if you swiped your finger up or down
        // and then if you did, the app executes the action for a swipe up or down.
        // If you swiped up or down, the onclicklistener does nothing.
        // Otherwise, if you tapped on the button, the onclick listener adds the number
        // to the textbox.
        val numButton1 = findViewById<Button>(R.id.keypad_num1)
        numButton1.setOnTouchListener { _, event ->
            Log.i(DEBUG_TAG, "numButton1: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = findViewById<TextView>(R.id.lettera).text.toString()
            val swipeDownText = ""

            if (onFlingDetected != "no") {
                addNum(numButton1, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton1.setOnClickListener {
            Log.i(DEBUG_TAG, "numButton1: onTouchListener Called")
            if (!touchEvent) {
                Log.i(DEBUG_TAG, "numButton2: clicked.")
                Log.i(DEBUG_TAG, "numButton2: addNum(numButton4) called")
                addNum(numButton1)
            } else {
                touchEvent = false
            }
        }

        val numButton2 = findViewById<Button>(R.id.keypad_num2)
        numButton2.setOnTouchListener { _, event ->
            Log.i(DEBUG_TAG, "numButton2: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = findViewById<TextView>(R.id.letterb).text.toString()
            val swipeDownText = ""

            if (onFlingDetected != "no") {
                addNum(numButton2, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton2.setOnClickListener {
            Log.i(DEBUG_TAG, "numButton2: onTouchListener Called")
            if (!touchEvent) {
                Log.i(DEBUG_TAG, "numButton2: clicked.")
                Log.i(DEBUG_TAG, "numButton2: addNum(numButton4) called")
                addNum(numButton2)
            } else {
                touchEvent = false
            }
        }

        val numButton3 = findViewById<Button>(R.id.keypad_num3)
        numButton3.setOnTouchListener { _, event ->
            Log.i(DEBUG_TAG, "numButton3: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = findViewById<TextView>(R.id.letterc).text.toString()
            val swipeDownText = ""

            if (onFlingDetected != "no") {
                addNum(numButton3, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton3.setOnClickListener {
            Log.i(DEBUG_TAG, "numButton3: onTouchListener Called")
            if (!touchEvent) {
                Log.i(DEBUG_TAG, "numButton3: clicked.")
                Log.i(DEBUG_TAG, "numButton3: addNum(numButton4) called")
                addNum(numButton3)
            }
        }

        val numButton4 = findViewById<Button>(R.id.keypad_num4)
        numButton4.setOnTouchListener { _, event ->
            Log.i(DEBUG_TAG, "numButton4: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = findViewById<TextView>(R.id.comma).text.toString()
            val swipeDownText = findViewById<TextView>(R.id.B1R0).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton4, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton4.setOnClickListener {
            Log.i(DEBUG_TAG, "numButton4: onTouchListener Called")
            if (!touchEvent) {
                Log.i(DEBUG_TAG, "numButton4: clicked.")
                Log.i(DEBUG_TAG, "numButton4: addNum(numButton4) called")
                addNum(numButton4)
            }
        }


        val numButton5 = findViewById<Button>(R.id.keypad_num5)
        numButton5.setOnTouchListener { _, event ->
            Log.i(DEBUG_TAG, "numButton5: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = "-"
            val swipeDownText = findViewById<TextView>(R.id.B2R0).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton5, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton5.setOnClickListener {
            Log.i(DEBUG_TAG, "numButton5: onTouchListener Called")
            if (!touchEvent) {
                Log.i(DEBUG_TAG, "numButton5: clicked.")
                Log.i(DEBUG_TAG, "numButton5: addNum(numButton5) called")
                addNum(numButton5)
            }
        }

        val numButton6 = findViewById<Button>(R.id.keypad_num6)
        numButton6.setOnTouchListener { _, event ->
            Log.i(DEBUG_TAG, "numButton6: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)

            val swipeUpText = findViewById<TextView>(R.id.semicolon).text.toString()
            val swipeDownText = findViewById<TextView>(R.id.B3R0).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton6, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton6.setOnClickListener {
            Log.i(DEBUG_TAG, "numButton6: onTouchListener Called")
            if (!touchEvent) {
                Log.i(DEBUG_TAG, "numButton6: clicked.")
                Log.i(DEBUG_TAG, "numButton6: addNum(numButton6) called")
                addNum(numButton6)
            }
        }

        val numButton7 = findViewById<Button>(R.id.keypad_num7)
        numButton7.setOnTouchListener { _, event ->
            Log.i(DEBUG_TAG, "numButton7: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = ""
            val swipeDownText = findViewById<TextView>(R.id.B1R1).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton7, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton7.setOnClickListener {
            Log.i(DEBUG_TAG, "numButton7: onTouchListener Called")
            if (!touchEvent) {
                Log.i(DEBUG_TAG, "numButton7: clicked.")
                Log.i(DEBUG_TAG, "numButton7: addNum(numButton7) called")
                addNum(numButton7)
          }
        }


        val numButton8 = findViewById<Button>(R.id.keypad_num8)
        numButton8.setOnTouchListener { _, event ->
            Log.i(DEBUG_TAG, "numButton8: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = ""
            val swipeDownText = findViewById<TextView>(R.id.B2R1).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton8, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton8.setOnClickListener {
            Log.i(DEBUG_TAG, "numButton8: onTouchListener Called")
            if (!touchEvent) {
                Log.i(DEBUG_TAG, "numButton8: clicked.")
                Log.i(DEBUG_TAG, "numButton8: addNum(numButton8) called")
                Log.i("keypad button ${numButton8.text}", "Clicked")
                addNum(numButton8)
            }
        }


        val numButton9 = findViewById<Button>(R.id.keypad_num9)
        numButton9.setOnTouchListener { _, event ->
            Log.i(DEBUG_TAG, "numButton9: onTouchListener Called")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = ""
            val swipeDownText = findViewById<TextView>(R.id.B3R1).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton9, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton9.setOnClickListener {
            Log.i(DEBUG_TAG, "numButton9: onClickListener Called")
          if (!touchEvent) {
              Log.i(DEBUG_TAG, "numButton9: clicked.")
              Log.i(DEBUG_TAG, "numButton9: addNum(numButton9) called")
              addNum(numButton9)
          }
        }


        val numButton0 = findViewById<Button>(R.id.keypad_num0)
        numButton0.setOnTouchListener { _, event ->
            touchEvent = false
            onFlingDetected = "no"
            Log.w("key0", "ontouchactivated")
            gestureDetector.onTouchEvent(event)

            if (onFlingDetected == "up") {
                modStreetName()
                touchEvent = true
                Log.i(DEBUG_TAG, "numButton0: onFlingDetected = up, touchevent = true")
            } else if (onFlingDetected == "down") {
                modBuildLevels()
                touchEvent = true
                Log.i(DEBUG_TAG, "numButton0: onFlingDetected = up, touchevent = true")
            } else {
                touchEvent = false
                Log.w(DEBUG_TAG, "numButton0: onFlingDetected != up || down, touchEvent = false")
            }

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton0.setOnClickListener() {
            Log.i("key0", "onclicklistener called.")
            if (!touchEvent) {
                addNum(numButton0)
            }
        }


        val backspaceButton = findViewById<ImageButton>(R.id.backspace)
        backspaceButton.setOnTouchListener { _, event ->
            Log.i(DEBUG_TAG, "backspaceButton pressed")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)

            if (onFlingDetected == "up"){
                modifyIncrement()
            }

            return@setOnTouchListener super.onTouchEvent(event)
        }

        // go back to MainActivity & send address data to MainActivity
        val doneButton = findViewById<ImageButton>(R.id.done)
        doneButton.setOnClickListener {

            val addressTextbox = findViewById<EditText>(R.id.address_textbox)
            val lat = intent.getDoubleExtra("lat", 0.000)
            val lon = intent.getDoubleExtra("lon", 0.000)
            val side = intent.getStringExtra("side").toString()
            Log.i(DEBUG_TAG, "address number: ${addressTextbox.text}")

            buildingLevels = findViewById<TextView>(R.id.building_levels_value).text.toString()
            val address = AddressNodes(addressTextbox.text.toString(), street, lat, lon,
                    side, buildingLevels)

            val intent = Intent(this, MainActivity::class.java)

            intent.putExtra("address", address)

            setResult(RESULT_OK, intent)
            finish()
        }

        val cancelButton = findViewById<ImageButton>(R.id.remove)
        cancelButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Modify the default increment used when you double tap or long press on the right/left arrow
    // in the MainActivity. For example, if your last entered housenumber was "24", and the
    // increment is "-3", then the next housenumber you add will be "21".
    // Default increment is 2.
    private fun modifyIncrement() {
        val incrementButtonDimensions = 200
        val textBoxWidth = 200
        val textBoxTextSize = 40f

        val sharedPreferences = getPreferences(MODE_PRIVATE)
        increment = sharedPreferences.getInt("increment", 2)

        Log.i(DEBUG_TAG, "Increment before function: $increment")
        val modifyIncrementDialogue  = AlertDialog.Builder(this)
        modifyIncrementDialogue.setTitle(getString(R.string.change_increment))

        var incrementValue : String

        val modifyIncrementInput = EditText(this)
        modifyIncrementInput.inputType = EditorInfo.TYPE_CLASS_PHONE
        modifyIncrementInput.setText(increment.toString())
        modifyIncrementInput.textSize = textBoxTextSize
        modifyIncrementInput.layoutParams = ViewGroup.LayoutParams(textBoxWidth, MATCH_PARENT)
        modifyIncrementInput.gravity =CENTER

        val minusButton = ImageButton(this)
        minusButton.setImageResource(R.drawable.minus)

        minusButton.layoutParams = ViewGroup.LayoutParams(incrementButtonDimensions,
                incrementButtonDimensions)
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
        plusButton.layoutParams = ViewGroup.LayoutParams(incrementButtonDimensions,
                incrementButtonDimensions)
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

        modifyIncrementDialogue.setPositiveButton(getString(R.string.save_increment)) {
            _, _ -> incrementValue = modifyIncrementInput.text.toString()
            try {
                Log.i(DEBUG_TAG, "incrementValue in Dialog: $incrementValue")
                increment = incrementValue.toInt()

                val sharedPreferencesEditor = getPreferences(MODE_PRIVATE).edit()
                sharedPreferencesEditor.putInt("increment", increment)
                sharedPreferencesEditor.apply()
            } catch (e: TypeCastException) {
                e.printStackTrace()
                Log.e(DEBUG_TAG, getString(R.string.increment_not_integer))
                Toast.makeText(this, getString(R.string.increment_not_integer),
                        Toast.LENGTH_SHORT).show()
            }
        }
        modifyIncrementDialogue.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
        modifyIncrementDialogue.create().show()
    }


    // Finds the nearest street to the position of the housenumber that you want to add.
    // Uses Nominatim reverse geocoder to do this.
    @SuppressLint("SetTextI18n")
    private fun reverseGeocodeStreet(lat: Double, lon: Double) {
        var nominatimReverseGeocode : URL
        Thread {
            nominatimReverseGeocode = URL(
                    "https://nominatim.openstreetmap.org/reverse?format=xml&lat=$lat&lon=$lon")
            val result = nominatimReverseGeocode.readText()
            Log.i("result:", result)
            val streetName: String
            try {
                streetName = StringUtils.substringBetween(result, "<road>", "</road>")
                street = streetName
                Log.i("street name: ", streetName)

            } catch (e: NullPointerException) {
                val lastAddress = intent.getParcelableExtra<AddressNodes?>("last_address")
                Log.i(DEBUG_TAG, "nullPointerException when trying to geocode street.")

                if (lastAddress != null) {
                    street = lastAddress.street

                    val streetNameTextView = findViewById<TextView>(R.id.street_name_value)
                    runOnUiThread {
                        if (street.length < 18) {
                            streetNameTextView.text = street
                        } else {
                            streetNameTextView.text = "${street.subSequence(0, 15)}..."
                        }
                    }
                } else {
                    street = ""
                }
            }

            runOnUiThread {
                val streetNameTextView = findViewById<TextView>(R.id.street_name_value)
                if (street.length < 18) {
                    Log.i("stgert", "in if statement")
                    streetNameTextView.text = street
                } else {
                    Log.i("in else statement", street.subSequence(0, 15) as String)
                    streetNameTextView.text = "${street.subSequence(0, 15) as String}..."
                }
            }
        }.start()

    }

    // Change the building levels of this address
    private fun modBuildLevels() {
        // TODO : Implement
        Toast.makeText(this, getString(R.string.unimplemented),
                Toast.LENGTH_SHORT).show()
    }

    // Change the street name corresponding to this address.
    // This information should stay the same unless manually changed.
    @SuppressLint("SetTextI18n")
    private fun modStreetName() {
        val changeStreetDialogue  = AlertDialog.Builder(this)
        changeStreetDialogue.setTitle(getString(R.string.street_name))

        var streetNameValue : String
        val streetNameInput = EditText(this)

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

            val streetNameTextView = findViewById<TextView>(R.id.street_name_value)

            if (streetNameValue.length < 18) {
                streetNameTextView.text = streetNameValue
            } else {
                streetNameTextView.text = "${streetNameValue.subSequence(0, 15)}..."
            }

            street = streetNameValue
        }
        changeStreetDialogue.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
        changeStreetDialogue.create().show()
    }

    // Adds value of number pressed to textbox.
    @SuppressLint("SetTextI18n")
    private fun addNum(numButton : Button, swipeUpText : String, swipeDownText : String) {
        val textbox = findViewById<EditText>(R.id.address_textbox)


        if (onFlingDetected == "up") {
            if (swipeUpText != "") {

                textbox.setText("${textbox.text}${swipeUpText}")
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
                val buildingLevelsValue = findViewById<TextView>(R.id.building_levels_value)
                buildingLevelsValue.text = swipeDownText
            }
        } else {
            Log.e("addNum()", "failed, onFlingDetected=$onFlingDetected")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addNum(numButton: Button) {
        val textbox = findViewById<EditText>(R.id.address_textbox)

        textbox.setText("${textbox.text}${numButton.text}")
    }

    // Clears the textbox
    private fun clearTextbox() {
        val addressTextbox = findViewById<EditText>(R.id.address_textbox)
        addressTextbox.text.clear()
    }

    // Removes last character from textbox (after pressing "backspace")
    fun textboxRemoveLastChar(view: View) {

        val addressTextbox = findViewById<EditText>(R.id.address_textbox)
        var string = addressTextbox.text.toString()

        string = string.dropLast(1)
        addressTextbox.setText(string)
    }

    // Increments the number in the textbox by one
    // Firstly, all characters are removed, then the remaining number is incremented
    fun incrementTextbox(view: View) {

        val addressTextbox = findViewById<EditText>(R.id.address_textbox)
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
                addressTextbox.setText(numToIncrement.toString())
            } catch (e: NumberFormatException) {
                var textToSet = "";

                for (c in addressTextboxText) {
                    val intOrNot = c.toString().toIntOrNull()
                    if (intOrNot != null) {
                        textToSet += intOrNot.toString()
                        Log.i(textToSet, "text to set")
                    }
                }
                textToSet = (textToSet.toInt() + 1).toString()
                addressTextbox.setText(textToSet)

                Log.i(textToSet, "final text")
            }
        } else if (addressTextboxHint != "") {
            try {
                var numToIncrement = addressTextboxHint.toInt()
                numToIncrement += 1
                addressTextbox.setText(numToIncrement.toString())
            } catch (e: NumberFormatException) {
                var textToSet = "";

                for (c in addressTextboxHint) {
                    val intOrNot = c.toString().toIntOrNull()
                    if (intOrNot != null) {
                        textToSet += intOrNot.toString()
                        Log.i(textToSet, "text to set")
                    }
                }
                textToSet = (textToSet.toInt() + 1).toString()
                addressTextbox.setText(textToSet)

                Log.i(textToSet, "final text")
            }
        }
    }

    // Decrements the number in the textbox by one
    // Firstly, all characters are removed, then the remaining number is incremented
    fun decrementTextbox(view: View) {

        val addressTextbox = findViewById<EditText>(R.id.address_textbox)
        val addressTextboxText = addressTextbox.text.toString()
        var addressTextboxHint: String
        try {
            addressTextboxHint = addressTextbox.hint.toString()
        } catch (e : NullPointerException) {
            addressTextboxHint = ""
        }
        if (addressTextboxText != "") {
            try {
                var num_to_decrement = addressTextboxText.toInt()
                num_to_decrement -= 1
                addressTextbox.setText(num_to_decrement.toString())
            } catch (e: NumberFormatException) {
                var textToSet = "";
                if (addressTextboxText.isNotBlank()) {
                    for (c in addressTextboxText) {
                        val intOrNot = c.toString().toIntOrNull()
                        if (intOrNot != null) {
                            textToSet += intOrNot.toString()
                            Log.i(textToSet, "text to set")
                        }
                    }
                    textToSet = (textToSet.toInt() - 1).toString()
                    addressTextbox.setText(textToSet)
                }
                Log.i(textToSet, "final text")
            }
        } else if (addressTextboxHint != "") {
            try {
                var numToDecrement = addressTextboxHint.toInt()
                numToDecrement -= 1
                addressTextbox.setText(numToDecrement.toString())
            } catch (e: NumberFormatException) {
                var textToSet = "";

                for (c in addressTextboxHint) {
                    val intOrNot = c.toString().toIntOrNull()
                    if (intOrNot != null) {
                        textToSet += intOrNot.toString()
                        Log.i(textToSet, "text to set")
                    }
                }
                textToSet = (textToSet.toInt() - 1).toString()
                addressTextbox.setText(textToSet)

                Log.i(textToSet, "final text")
            }
        }
    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

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
        Log.i("Keypad", "onFlingDetected")

        val swipeValue = 100

        val differenceInY = moveEvent.y - downEvent.y
        val differenceInX = moveEvent.x - downEvent.x

        if (Math.abs(differenceInY) > Math.abs(differenceInX)) {
            // This is an up or down swipe

            if(Math.abs(differenceInY) > swipeValue) {
                if (differenceInY < 0) {
                    // up swipe
                    Log.i("onFlingDetected", "up swipe")
                    this.onFlingDetected = "up"
                } else {
                    // down swipe
                    Log.i("onFlingDetected", "down swipe")
                    this.onFlingDetected = "down"
                }
            }
        }

        return true


    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return false
    }

}
