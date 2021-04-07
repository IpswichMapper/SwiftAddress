package com.mapitall.SwiftAddress

import android.content.Context
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.core.widget.addTextChangedListener
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

class PolylineMarkerWindow(private val mapClass: Map,
                           private val context: Context,
                           private val ID: Int,
                           private val mainActivity: MainActivity) :
        InfoWindow(R.layout.polyline_press_layout_linear, mapClass.mapView) {

    private lateinit var storeHouseNumbersObject: StoreHouseNumbers
    private lateinit var saveButton: Button
    private lateinit var interpolation: String
    private lateinit var inclusion: String
    private lateinit var interpolationEditText: InstantAutoComplete
    private lateinit var inclusionDropDown: Spinner
    private lateinit var interpolationOptionsList: List<String>

    private val TAG = "PolylineMarkerWindow"

    override fun onOpen(item: Any?) {

        storeHouseNumbersObject = StoreHouseNumbers(context)
        val deleteButton = mView.findViewById<Button>(R.id.delete_polyline_linear)
        saveButton = mView.findViewById(R.id.save_polyline_linear)

        deleteButton.setOnClickListener {
            mapClass.removePolylineAt(ID)
            close()
        }
        saveButton.setOnClickListener {
            saveChanges()
            close()
        }

        val textView: TextView = mView.findViewById(R.id.polyline_text_view)
        val houseNumberPair = storeHouseNumbersObject.getPolylineHouseNumbers(ID)
        val startHouseNumber = houseNumberPair.first
        val endHouseNumber = houseNumberPair.second

        textView.append(context.getString(R.string.first_house_number) + " ")
        textView.append(HtmlCompat.fromHtml("<b>$startHouseNumber</b><br>", 0))
        textView.append(context.getString(R.string.last_house_number) + " ")
        textView.append(HtmlCompat.fromHtml("<b>$endHouseNumber</b><br>", 0))

        val polylineDetailsPair = storeHouseNumbersObject.getPolylineDetails(ID)
        interpolation = polylineDetailsPair.first
        inclusion = polylineDetailsPair.second

        interpolationEditText = mView.findViewById(
                R.id.interpolation_edit_text_linear)
        inclusionDropDown = mView.findViewById(
                R.id.inclusion_edit_text_linear)

        interpolationOptionsList = listOf("even", "odd", "alphabetic", "all")
        interpolationEditText.setAdapter(ArrayAdapter(
                context,
                android.R.layout.simple_dropdown_item_1line,
                interpolationOptionsList))

        interpolationEditText.setText(interpolation)

        inclusionDropDown.adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("", "actual", "estimate", "potential"))

        when(inclusion) {
            "actual" -> inclusionDropDown.setSelection(1)
            "estimate" -> inclusionDropDown.setSelection(2)
            "potential" -> inclusionDropDown.setSelection(3)
            else  -> inclusionDropDown.setSelection(0)
        }

        interpolationEditText.addTextChangedListener {
            setSaveButton()
        }
        inclusionDropDown.onItemSelectedListener = object:AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setSaveButton()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

    }

    override fun onClose() {
        try {
            mapClass.getPolyline(ID) // Simply to active catch block if marker is deleted
        } catch (e: NoSuchElementException) {
            Log.i(TAG, "Marker was deleted")
        } finally {
            Log.i(TAG, "InfoWindow Closed.")
        }
        // TODO: Add ability to hide keyboard
    }

    private fun setSaveButton() {
        if (interpolationEditText.text.toString() in interpolationOptionsList
                    && (interpolationEditText.text.toString() != interpolation
                            || inclusionDropDown.selectedItem.toString() != inclusion)) {
                setSaveButton(true)
            } else {
                try {
                    interpolationEditText.text.toString().toInt()
                    if (interpolationEditText.text.toString() != interpolation
                            || inclusionDropDown.selectedItem.toString() != inclusion) {
                        setSaveButton(true)
                    } else {
                        setSaveButton(false)
                    }
                } catch (e : NumberFormatException) {
                    setSaveButton(false)
                }
            }
    }

    private fun setSaveButton(enabled: Boolean) {
        if (enabled) {
            saveButton.isEnabled = true
            saveButton.alpha = 1f
        } else {
            saveButton.isEnabled = false
            saveButton.alpha = 0.5f
            Log.i(TAG, "Save Button Disabled")
        }
    }

    private fun saveChanges() {
        interpolation = interpolationEditText.text.toString()
        inclusion = inclusionDropDown.selectedItem.toString()
        storeHouseNumbersObject.changePolyline(ID, interpolation, inclusion)
    }

}