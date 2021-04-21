package com.mapitall.SwiftAddress

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.Gravity.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.preference.PreferenceManager
import org.apache.commons.lang3.StringUtils
import java.net.URL
import java.util.*
import kotlin.math.abs

class Keypad : AppCompatActivity(),
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private lateinit var gestureDetector: GestureDetectorCompat
    private var onFlingDetected = "no"
    private var touchEvent = false
    private var street = ""
    private var houseName = ""
    private var buildingLevels = ""
    private var increment  = 2

    private val TAG = "Keypad"

    @SuppressLint("ClickableViewAccessibility", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keypad)

        supportActionBar?.hide()


        val lastAddress = intent.getParcelableExtra<AddressNodes?>("last_address")

        gestureDetector = GestureDetectorCompat(this, this)

        val texbox = findViewById<EditText>(R.id.address_textbox)

        // Set hint to be housenumber of last address
        // Set street to be the street of last andress

        if (lastAddress != null) {
            texbox.hint = lastAddress.housenumber
        }

        val sp = PreferenceManager.getDefaultSharedPreferences(this)

        val netInfo = (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)
                .activeNetworkInfo

        if (netInfo != null && netInfo.isConnected &&
                sp.getBoolean("useReverseGeocoding", true) &&
                sp.getBoolean("online-queries", true)) {
            reverseGeocodeStreet(intent.getDoubleExtra(
                    "lat", 0.000), intent.getDoubleExtra("lon", 0.000))
        } else {
            val streetNameTextView = findViewById<TextView>(R.id.street_name_value)

            if (lastAddress != null) {
                street = lastAddress.street
                streetNameTextView.text = street
            }
            streetNameTextView.setTextColor(
                    ContextCompat.getColor(this, R.color.button_colors))
        }

        val addressTextBox = findViewById<EditText>(R.id.address_textbox)
        addressTextBox.requestFocus()

        addressTextBox.setOnLongClickListener {

            val vibrator: Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(
                        VibrationEffect.createOneShot(
                                80L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(80L)
            }

            addHouseName()
            return@setOnLongClickListener true
        }

        Log.i("street outside", street)

        uppercaseOrLowercaseLetters()

        // Onclick listeners and ontouch listeners for the keypad buttons
        // "Ontouch" is activated when you do any action on the buttons, for example touch or swipe
        // After "ontouch", the code checks if you swiped your finger up or down
        // and then if you did, the app executes the action for a swipe up or down.
        // If you swiped up or down, the onclicklistener does nothing.
        // Otherwise, if you tapped on the button, the onclick listener adds the number
        // to the textbox.
        val numButton1 = findViewById<Button>(R.id.keypad_num1)
        numButton1.setOnTouchListener { _, event ->
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
            if (!touchEvent) {
                Log.i(TAG, "numButton1: clicked.")
                addNum(numButton1)
            } else {
                touchEvent = false
            }
        }

        val numButton2 = findViewById<Button>(R.id.keypad_num2)
        numButton2.setOnTouchListener { _, event ->
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
            if (!touchEvent) {
                Log.i(TAG, "numButton2: clicked.")
                addNum(numButton2)
            } else {
                touchEvent = false
            }
        }

        val numButton3 = findViewById<Button>(R.id.keypad_num3)
        numButton3.setOnTouchListener { _, event ->
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
            if (!touchEvent) {
                Log.i(TAG, "numButton3: clicked.")
                addNum(numButton3)
            }
        }

        val numButton4 = findViewById<Button>(R.id.keypad_num4)
        numButton4.setOnTouchListener { _, event ->
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = findViewById<TextView>(R.id.letterd).text.toString()
            val swipeDownText = findViewById<TextView>(R.id.B1R0).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton4, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton4.setOnClickListener {
            if (!touchEvent) {
                Log.i(TAG, "numButton4: clicked.")
                addNum(numButton4)
            }
        }


        val numButton5 = findViewById<Button>(R.id.keypad_num5)
        numButton5.setOnTouchListener { _, event ->
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = findViewById<TextView>(R.id.lettere).text.toString()
            val swipeDownText = findViewById<TextView>(R.id.B2R0).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton5, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton5.setOnClickListener {
            if (!touchEvent) {
                Log.i(TAG, "numButton5: clicked.")
                addNum(numButton5)
            }
        }

        val numButton6 = findViewById<Button>(R.id.keypad_num6)
        numButton6.setOnTouchListener { _, event ->
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)

            val swipeUpText = findViewById<TextView>(R.id.letterf).text.toString()
            val swipeDownText = findViewById<TextView>(R.id.B3R0).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton6, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton6.setOnClickListener {
            if (!touchEvent) {
                Log.i(TAG, "numButton6: clicked.")
                addNum(numButton6)
            }
        }

        val numButton7 = findViewById<Button>(R.id.keypad_num7)
        numButton7.setOnTouchListener { _, event ->
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = findViewById<TextView>(R.id.comma).text.toString()
            val swipeDownText = findViewById<TextView>(R.id.B1R1).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton7, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton7.setOnClickListener {
            if (!touchEvent) {
                Log.i(TAG, "numButton7: clicked.")
                addNum(numButton7)
          }
        }


        val numButton8 = findViewById<Button>(R.id.keypad_num8)
        numButton8.setOnTouchListener { _, event ->
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = "-"
            val swipeDownText = findViewById<TextView>(R.id.B2R1).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton8, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton8.setOnClickListener {
            if (!touchEvent) {
                Log.i(TAG, "numButton8: clicked.")
                addNum(numButton8)
            }
        }


        val numButton9 = findViewById<Button>(R.id.keypad_num9)
        numButton9.setOnTouchListener { _, event ->
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)
            val swipeUpText = findViewById<TextView>(R.id.semicolon).text.toString()
            val swipeDownText = findViewById<TextView>(R.id.B3R1).text.toString()

            if (onFlingDetected != "no") {
                addNum(numButton9, swipeUpText, swipeDownText)
                touchEvent = true
            } else touchEvent = false

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton9.setOnClickListener {
          if (!touchEvent) {
              Log.i(TAG, "numButton9: clicked.")
              addNum(numButton9)
          }
        }


        val numButton0 = findViewById<Button>(R.id.keypad_num0)
        numButton0.setOnTouchListener { _, event ->
            touchEvent = false
            onFlingDetected = "no"
            gestureDetector.onTouchEvent(event)
            if (onFlingDetected == "up") {
                modStreetName()
                touchEvent = true
            } else if (onFlingDetected == "down") {
                modBuildLevels()
                touchEvent = true
            } else {
                touchEvent = false
            }

            return@setOnTouchListener super.onTouchEvent(event)
        }
        numButton0.setOnClickListener {
            Log.i(TAG, "numButton0: onClickListener called.")
            if (!touchEvent) {
                addNum(numButton0)
            }
        }


        val backspaceButton = findViewById<ImageButton>(R.id.backspace)
        // Modify increment dialogue shows if you swipe up on backspace button
        backspaceButton.setOnTouchListener { _, event ->
            Log.i(TAG, "backspaceButton pressed")
            touchEvent = false
            onFlingDetected = "no"

            gestureDetector.onTouchEvent(event)

            if (onFlingDetected == "up") {
                modifyIncrement()
            }
            if (onFlingDetected == "down") {
                addHouseName()
            }

            return@setOnTouchListener super.onTouchEvent(event)
        }
        // clear address textbox when backspace is long pressed
        backspaceButton.setOnLongClickListener(View.OnLongClickListener {
            clearTextbox()
            return@OnLongClickListener true
        })

        val streetNameTag = findViewById<TextView>(R.id.street_name_tag)
        val streetNameValue = findViewById<TextView>(R.id.street_name_value)
        streetNameTag.setOnClickListener {
            modStreetName()
        }
        streetNameValue.setOnClickListener {
            modStreetName()
        }

        // go back to MainActivity & send address data to MainActivity
        val doneButton = findViewById<ImageButton>(R.id.done)
        doneButton.setOnClickListener {

            val addressTextbox = findViewById<EditText>(R.id.address_textbox)
            val lat = intent.getDoubleExtra("lat", 0.000)
            val lon = intent.getDoubleExtra("lon", 0.000)
            val side = intent.getStringExtra("side").toString()

            buildingLevels = findViewById<TextView>(R.id.building_levels_value).text.toString()
            val address = AddressNodes(addressTextbox.text.toString(), lat, lon,
                    side, street, buildingLevels, houseName)

            val intent = Intent(this, MainActivity::class.java)

            intent.putExtra("address", address)

            Log.i(TAG, "address: $address")

            setResult(RESULT_OK, intent)
            finish()
        }

        val cancelButton = findViewById<ImageButton>(R.id.remove)
        cancelButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)

            setResult(RESULT_CANCELED, intent)
            finish()
        }
    }

    private fun uppercaseOrLowercaseLetters() {
        val a = findViewById<TextView>(R.id.lettera)
        val b = findViewById<TextView>(R.id.letterb)
        val c = findViewById<TextView>(R.id.letterc)
        val d = findViewById<TextView>(R.id.letterd)
        val e = findViewById<TextView>(R.id.lettere)
        val f = findViewById<TextView>(R.id.letterf)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val uppercase = sp.getBoolean("uppercase_letters", false)

        if (uppercase) {
            a.text = getString(R.string.letter_a_uppercase)
            b.text = getString(R.string.letter_b_uppercase)
            c.text = getString(R.string.letter_c_uppercase)
            d.text = getString(R.string.letter_d_uppercase)
            e.text = getString(R.string.letter_e_uppercase)
            f.text = getString(R.string.letter_f_uppercase)
        } else {
            a.text = getString(R.string.letter_a_lowercase)
            b.text = getString(R.string.letter_b_lowercase)
            c.text = getString(R.string.letter_c_lowercase)
            d.text = getString(R.string.letter_d_lowercase)
            e.text = getString(R.string.letter_e_lowercase)
            f.text = getString(R.string.letter_f_lowercase)
        }
    }

    private fun addHouseName() {

        val addHouseNameDialog = AlertDialog.Builder(this)
        addHouseNameDialog.setTitle(getString(R.string.house_name))

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

        addHouseNameDialog.setPositiveButton(getString(R.string.add_house_name)) { _, _ ->
            houseName = addHouseNameEditText.text.toString()
        }
        addHouseNameDialog.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
        val dialog = addHouseNameDialog.create()
        dialog.show()
        addHouseNameEditText.requestFocus()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

    }

    // Modify the default increment used when you double tap or long press on the right/left arrow
    // in the MainActivity. For example, if your last entered housenumber was "24", and the
    // increment is "-3", then the next housenumber you add will be "21".
    // Default increment is 2.
    private fun modifyIncrement() {
        val incrementButtonDimensions = 200
        val textBoxWidth = 200
        val textBoxTextSize = 40f

        // val sharedPreferences = getSharedPreferences(getString(R.string.preference_string), MODE_PRIVATE)
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        increment = sp.getInt("increment", 2)

        Log.i(TAG, "Increment before function: $increment")
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
                    var textToSet = ""

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
            Log.i(TAG, "modifyIncrementDialog: minusButton pressed")
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
                    var textToSet = ""

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

        modifyIncrementDialogue.setPositiveButton(getString(R.string.save_increment)) {
            _, _ -> incrementValue = modifyIncrementInput.text.toString()
            try {
                Log.i(TAG, "incrementValue in Dialog: $incrementValue")
                increment = incrementValue.toInt()

                val sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(
                        this).edit()
                sharedPreferencesEditor.putInt("increment", increment)
                sharedPreferencesEditor.apply()
            } catch (e: TypeCastException) {
                e.printStackTrace()
                Log.e(TAG, getString(R.string.increment_not_integer))
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
    fun reverseGeocodeStreet(lat: Double, lon: Double) {
        var nominatimReverseGeocode : URL
        Thread {
            nominatimReverseGeocode = URL(
                    "https://nominatim.openstreetmap.org/reverse?format=xml&lat=$lat&lon=$lon")
            val result = nominatimReverseGeocode.readText()
            Log.i("result:", result)
            var sameAsLastAddress = false
            val streetName: String
            val lastAddress = intent.getParcelableExtra<AddressNodes?>("last_address")

            try {
                streetName = StringUtils.substringBetween(result, "<road>", "</road>")
                street = streetName
                Log.i("street name: ", streetName)

            } catch (e: NullPointerException) {

                Log.i(TAG, "nullPointerException when trying to geocode street.")
                if (lastAddress != null) {
                    street = lastAddress.street
                } else {
                    street = ""
                }
            }

            runOnUiThread {
                val streetNameTextView = findViewById<TextView>(R.id.street_name_value)
                streetNameTextView.text = street
                // If the current street name is the same as the previous one,
                // the color of the street name text becomes green.
                // Otherwise it becomes red.
                if (lastAddress != null) {
                    if (street == lastAddress.street) {
                        streetNameTextView.setTextColor(
                            ContextCompat.getColor(this, R.color.street_name_previous))
                    } else {
                        streetNameTextView.setTextColor(
                            ContextCompat.getColor(this, R.color.street_name_new))
                    }
                } else {
                    streetNameTextView.setTextColor(
                        ContextCompat.getColor(this, R.color.button_colors))
                }
            }
        }.start()

    }

    // Change the building levels of this address
    @SuppressLint("SetTextI18n")
    private fun modBuildLevels() {

        val modifyBuildLevelsDialog = AlertDialog.Builder(this)

        modifyBuildLevelsDialog.setTitle(getString(R.string.modify_building_levels))
        modifyBuildLevelsDialog.setMessage(getString(R.string.building_levels_question))

        var buildingLevelsValue : String
        var roofLevelsValue : String
        val buildingLevelsInput = EditText(this)
        val roofLevelsInput = EditText(this)

        buildingLevelsInput.hint = getString(R.string.building_levels_hint)
        roofLevelsInput.hint = getString(R.string.roof_levels_hint)

        buildingLevelsInput.inputType = InputType.TYPE_CLASS_PHONE
        roofLevelsInput.inputType = InputType.TYPE_CLASS_PHONE

        val container = FrameLayout(this)
        val params : FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            MATCH_PARENT, WRAP_CONTENT
        )

        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL

        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        linearLayout.layoutParams = params

        linearLayout.addView(buildingLevelsInput)
        linearLayout.addView(roofLevelsInput)
        container.addView(linearLayout)
        modifyBuildLevelsDialog.setView(container)
        
        modifyBuildLevelsDialog.setPositiveButton(getString(R.string.save)) { _, _ ->

            Log.i(TAG, "positive button pressed")
            try {
                buildingLevelsValue = buildingLevelsInput.text.toString()
                buildingLevelsValue.toInt()
                try {
                    roofLevelsValue = roofLevelsInput.text.toString()
                    roofLevelsValue.toInt()
                } catch (e: Exception) {
                    e.printStackTrace()

                    roofLevelsValue = "0"
                }
                Log.i(TAG, "about to change building levels")

                val buildingLevelsTextView = findViewById<TextView>(R.id.building_levels_value)
                buildingLevelsTextView.text = "B$buildingLevelsValue R$roofLevelsValue"
                buildingLevelsTextView.setTextColor(ContextCompat.getColor(
                        this, R.color.button_colors))
            } catch (e : Exception) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.please_enter_number), Toast.LENGTH_SHORT)
                    .show()
            }

        }
        modifyBuildLevelsDialog.setNeutralButton(getString(R.string.cancel)) { _, _ -> }



        val dialog = modifyBuildLevelsDialog.create()
        dialog.show()
        buildingLevelsInput.requestFocus()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

    }

    // Change the street name corresponding to this address.
    // This information should stay the same unless manually changed.
    @SuppressLint("SetTextI18n")
    private fun modStreetName() {
        val changeStreetDialogue  = AlertDialog.Builder(this)
        changeStreetDialogue.setTitle(getString(R.string.street_name))

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        var streetNameValue : String

        // Custom version of AutoCompleteTextView that always shows the dropdown
        // https://stackoverflow.com/a/5783983/15017966
        // See InstantAutoComplete class
        val streetNameInput = InstantAutoComplete(this)
        streetNameInput.maxLines = 1
        streetNameInput.inputType = InputType.TYPE_CLASS_TEXT


        val lat = intent.getDoubleExtra("lat", 0.000)
        val lon = intent.getDoubleExtra("lon", 0.000)
        val radius = 100

        if (sp.getBoolean("online-queries", true)) {
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
                    val array: Array<String> = StringUtils.substringsBetween(result,
                            "<tag k=\"name\" v=\"", "\"/>")
                    val distinctList = array.distinct()
                    Log.i(TAG, "list of completions: $distinctList")

                    runOnUiThread {

                        Log.i(TAG, "in runOnUiThread")
                        val arrayAdapter = ArrayAdapter(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                distinctList)
                        streetNameInput.setAdapter(arrayAdapter)
                        Log.i(TAG, "Query finished")
                        Toast.makeText(this, "Query finished", Toast.LENGTH_SHORT).show()

                        streetNameInput.showDropDown()

                    }
                } catch (e: NullPointerException) {
                    Log.i(TAG, "Failed to find any street names.")
                    Log.w(TAG, result)
                }

            }.start()
        }

        val container = FrameLayout(this)
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val params : LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            MATCH_PARENT, WRAP_CONTENT
        )
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        linearLayout.layoutParams = params

        val horizontalLinearLayout = LinearLayout(this)
        horizontalLinearLayout.orientation = LinearLayout.HORIZONTAL

        val checkBoxTextView = TextView(this)
        val checkBox = CheckBox(this)

        checkBoxTextView.layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 3f)
        checkBoxTextView.text = getString(R.string.disable_reverse_geocoding)
        checkBox.layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT, 0.5f)
        checkBox.gravity = CENTER_VERTICAL

        checkBox.isChecked = !sp.getBoolean("useReverseGeocoding", true)

        horizontalLinearLayout.isBaselineAligned = false
        horizontalLinearLayout.addView(checkBoxTextView)
        horizontalLinearLayout.addView(checkBox)

        linearLayout.addView(streetNameInput)
        linearLayout.addView(horizontalLinearLayout)

        container.addView(linearLayout)

        changeStreetDialogue.setView(container)
        changeStreetDialogue.setMessage(getString(R.string.remember_change_street_name))

        changeStreetDialogue.setPositiveButton(getString(R.string.change_street_name_button)) {
            _, _ -> streetNameValue = streetNameInput.text.toString()

            if (checkBox.isChecked) {
                sp.edit().putBoolean("useReverseGeocoding", false).apply()
            } else {
                sp.edit().putBoolean("useReverseGeocoding", true).apply()
            }

            street = streetNameValue
            val streetNameTextView = findViewById<TextView>(R.id.street_name_value)
            streetNameTextView.text = street
        }
        changeStreetDialogue.setNeutralButton(getString(R.string.cancel)) { _, _ -> }
        val dialog = changeStreetDialogue.create()
        dialog.show()
        streetNameInput.requestFocus()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

    }


    // Adds value of number pressed to textbox.
    @SuppressLint("SetTextI18n")
    private fun addNum(numButton : Button, swipeUpText : String, swipeDownText : String) {
        val textbox = findViewById<EditText>(R.id.address_textbox)


        if (onFlingDetected == "up") {
            if (swipeUpText != "") {

                textbox.append(swipeUpText)

                Log.i("addNum()", "$swipeUpText set, appended")
            } else {
                Log.w("onFling", "There is no action for \"swipe up\"")
            }
        } else if (onFlingDetected == "down") {
            Log.w("swipeDownText:", swipeDownText)
            if (swipeDownText == "") {
                Log.w("onFling", "There is no action for \"swipe down\"")
            } else {
                val buildingLevelsValue = findViewById<TextView>(R.id.building_levels_value)
                buildingLevelsValue.text = swipeDownText
                when (swipeDownText) {
                    getString(R.string.B1_R0) -> buildingLevelsValue.setTextColor(
                        ContextCompat.getColor(this, R.color.B1_R0))
                     getString(R.string.B2_R0) -> buildingLevelsValue.setTextColor(
                        ContextCompat.getColor(this, R.color.B2_R0))
                    getString(R.string.B3_R0) -> buildingLevelsValue.setTextColor(
                        ContextCompat.getColor(this, R.color.B3_R0))
                    getString(R.string.B1_R1) -> buildingLevelsValue.setTextColor(
                        ContextCompat.getColor(this, R.color.B1_R1))
                    getString(R.string.B2_R1) -> buildingLevelsValue.setTextColor(
                        ContextCompat.getColor(this, R.color.B2_R1))
                    getString(R.string.B3_R1) -> buildingLevelsValue.setTextColor(
                        ContextCompat.getColor(this, R.color.B3_R1))
                }
            }
        } else {
            Log.e("addNum()", "failed, onFlingDetected=$onFlingDetected")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addNum(numButton: Button) {
        val textbox = findViewById<EditText>(R.id.address_textbox)

        // textbox.setText("${textbox.text}${numButton.text}")
        textbox.append(numButton.text.toString())
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
        addressTextbox.text.clear()
        addressTextbox.append(string)
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
                addressTextbox.text.clear()
                addressTextbox.append(numToIncrement.toString())
            } catch (e: NumberFormatException) {
                var textToSet = ""

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

                Log.i(textToSet, "final text")
            }
        } else if (addressTextboxHint != "") {
            try {
                var numToIncrement = addressTextboxHint.toInt()
                numToIncrement += 1
                addressTextbox.text.clear()
                addressTextbox.append(numToIncrement.toString())
            } catch (e: NumberFormatException) {
                var textToSet = ""

                for (c in addressTextboxHint) {
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
        } else if (addressTextboxHint != "") {
            try {
                var numToDecrement = addressTextboxHint.toInt()
                numToDecrement -= 1
                addressTextbox.text.clear()
                addressTextbox.append(numToDecrement.toString())
            } catch (e: NumberFormatException) {
                var textToSet = ""

                for (c in addressTextboxHint) {
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

        val swipeValue = 100

        val differenceInY = moveEvent.y - downEvent.y
        val differenceInX = moveEvent.x - downEvent.x

        if (abs(differenceInY) > abs(differenceInX)) {
            // This is an up or down swipe

            if(abs(differenceInY) > swipeValue) {
                if (differenceInY < 0) {
                    // up swipe
                    this.onFlingDetected = "up"
                    Log.i(TAG, "onFlingDetected: $onFlingDetected")
                } else {
                    // down swipe
                    this.onFlingDetected = "down"
                    Log.i(TAG, "onFlingDetected: $onFlingDetected")
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

    fun addAddress(view: View) {}
    fun undo(view: View) {}
    fun takePhoto(view: View) {}
    fun saveData(view: View) {}
    fun openDrawer(view: View) {}

}
