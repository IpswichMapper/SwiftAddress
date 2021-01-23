package com.mapitall.SwiftAddress

import android.annotation.SuppressLint
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.*
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
                // TODO : Implement
                Toast.makeText(this, getString(R.string.unimplemented),
                        Toast.LENGTH_SHORT).show()
                touchEvent = true
            }

            return@setOnTouchListener super.onTouchEvent(event)
        }

        // go back to MainActivity & send address data to MainActivity
        findViewById<ImageButton>(R.id.done).setOnClickListener {

            val addressTextbox = findViewById<EditText>(R.id.address_textbox)
            val lat = intent.getDoubleExtra("lat", 0.000)
            val lon = intent.getDoubleExtra("lon", 0.000)
            val side = intent.getStringExtra("side").toString()
            Log.i("received data: Lat", lat.toString())
            Log.i("received data: Lon", lon.toString())
            Log.i("received data: Side", side)
            Log.i("data : address number", addressTextbox.text.toString())
            // TODO : make this work with streets.
            buildingLevels = findViewById<TextView>(R.id.building_levels_value).text.toString()
            val address = AddressNodes(addressTextbox.text.toString(), street, lat, lon,
                    side, buildingLevels)

            val intent = Intent(this, MainActivity::class.java)

            intent.putExtra("address", address)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun reverseGeocodeStreet(lat: Double, lon: Double) {
        var nominatimReverseGeocode : URL
        Thread {
            nominatimReverseGeocode = URL(
                    "https://nominatim.openstreetmap.org/reverse?format=xml&lat=$lat&lon=$lon")
            val result = nominatimReverseGeocode.readText()
            Log.i("result:", result)
            var streetName: String
            streetName = StringUtils.substringBetween(result, "<road>", "</road>")
            Log.i("street name: ", streetName)
            street = streetName

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
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
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

    // Increments the number in the textbox by one
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

    override fun onFling(downEvent: MotionEvent, moveEvent: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        Log.i("Keypad", "onFlingDetected")

        val swipeValue = 100
        val velocityValue = 100


        val differenceInY = moveEvent.y - downEvent.y
        val differenceInX = moveEvent.x - downEvent.x

        if (Math.abs(differenceInY) > Math.abs(differenceInX)) {
            // This is an up or down swipe

            if(Math.abs(differenceInY) > swipeValue && Math.abs(velocityY) > velocityValue) {
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
