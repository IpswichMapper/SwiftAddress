package com.mapitall.SwiftAddress

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.infowindow.InfoWindow

class AddressMarkerWindow(pressLayoutId : Int,
                          private val mapClass: Map,
                          private val context: Context,
                          private val ID : Int,
                          private val mainActivity: MainActivity,
                          private var houseNumber : String = "",
                          private var street : String = "") :
    InfoWindow(pressLayoutId, mapClass.mapView) {

    private val TAG = "MarkerWindow"
    private val storeHouseNumbersObject = StoreHouseNumbers(context)

    val houseNumberEditText = mView.findViewById<EditText>(R.id.housenumber_edit_text)
    val streetNameEditText = mView.findViewById<EditText>(R.id.street_name_edit_text)
    // val houseNameEditText = mView.findViewById<EditText>(R.id.housename_edit_text)

    @SuppressLint("SetTextI18n")
    override fun onOpen(item: Any?) {
        closeAllInfoWindowsOn(mapView)



        if (houseNumber != "") houseNumberEditText.setText(houseNumber)
        if (street != "") streetNameEditText.setText(street)

        
        Log.i(TAG, "InfoWindow Opened.")

        mView.setOnClickListener {
            close()
        }
        val moveButton = mView.findViewById<Button>(R.id.Move_linear)
        val interpolateButton = mView.findViewById<Button>(R.id.Interpolate_linear)
        val deleteButton = mView.findViewById<Button>(R.id.Delete_linear)

        if (!mainActivity.creatingInterpolationWay) {

            moveButton.setOnClickListener {

                val marker = mapClass.getMarker(ID)
                marker.position = mapClass.mapView.mapCenter as GeoPoint
                mainActivity.moveMarker(ID, marker)
                close()
            }
            interpolateButton.setOnClickListener {
                // Toast.makeText(context, R.string.unimplemented, Toast.LENGTH_SHORT).show()
                mapClass.createNewInterpolationWay(ID)
                close()
            }
            deleteButton.setOnClickListener {
                // Toast.makeText(context, R.string.unimplemented, Toast.LENGTH_SHORT).show()
                storeHouseNumbersObject.removeAt(ID)
                mapClass.removeAt(ID)
                close()
            }
        } else {
            moveButton.visibility = View.GONE
            interpolateButton.visibility = View.GONE
            deleteButton.visibility = View.GONE

            val finishInterpolation = mView.findViewById<Button>(R.id.finish_interpolation)
            finishInterpolation.visibility = View.VISIBLE

            finishInterpolation.setOnClickListener {

                val finishInterpolationDialogue = AlertDialog.Builder(context)

                val interpolationTextView = TextView(context)
                interpolationTextView.text = context.getString(R.string.addr_interpolation)
                interpolationTextView.setTextColor(ContextCompat.getColor(
                        context, R.color.button_colors))
                interpolateButton.setTextColor(ContextCompat.getColor(
                        context, R.color.button_colors))
                val interpolationEditText = AutoCompleteTextView(context)
                val interpolationOptionsList = listOf("even", "odd", "alphabetic", "all")
                interpolationEditText.setAdapter(ArrayAdapter(
                        context,
                        android.R.layout.simple_dropdown_item_1line,
                        interpolationOptionsList))

                val inclusionTextView = TextView(context)
                inclusionTextView.text = context.getString(R.string.addr_inclusion)
                inclusionTextView.setTextColor(ContextCompat.getColor(
                        context, R.color.button_colors))
                val inclusionDropDown = Spinner(context)
                inclusionDropDown.adapter = ArrayAdapter(
                        context,
                        android.R.layout.simple_spinner_dropdown_item,
                        listOf("", "actual", "estimate", "potential"))

                val linearLayout = LinearLayout(context)

                linearLayout.orientation = LinearLayout.VERTICAL
                linearLayout.addView(interpolationTextView)
                linearLayout.addView(interpolationEditText)
                linearLayout.addView(inclusionTextView)
                linearLayout.addView(inclusionDropDown)

                val container = FrameLayout(context)
                val params = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.leftMargin = context.resources.getDimensionPixelSize(
                        R.dimen.dialog_edit_text_margin)
                params.rightMargin = context.resources.getDimensionPixelOffset(
                        R.dimen.dialog_edit_text_margin)
                params.topMargin = context.resources.getDimensionPixelSize(
                        R.dimen.dialog_edit_text_margin)

                linearLayout.layoutParams = params
                container.addView(linearLayout)

                finishInterpolationDialogue.setView(container)

                Log.i(TAG, "inclusionDropDown: ${inclusionDropDown.selectedItem}")

                finishInterpolationDialogue.setPositiveButton("save") { _, _ ->
                    storeHouseNumbersObject.addInterpolationWay(mainActivity.startMarkerID!!,
                        mapClass.getPolyLineHashMapValue(mapClass.getPolyLineID(), true),
                        ID,
                        interpolationEditText.text.toString(),
                        inclusionDropDown.selectedItem.toString())
                    mapClass.finishInterpolationWay(ID)
                    mainActivity.creatingInterpolationWay = false
                    mainActivity.finishCreatingInterpolationWay()
                    closeAllInfoWindowsOn(mapClass.mapView)
                }
                finishInterpolationDialogue.setNeutralButton(
                        context.getString(R.string.cancel)) { _, _ -> }

                val dialog = finishInterpolationDialogue.create()
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

                interpolationEditText.addTextChangedListener {
                    if (interpolationEditText.text.toString() in interpolationOptionsList) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    } else {
                        try {
                            interpolationEditText.text.toString().toInt()
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        } catch (e : NumberFormatException) {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        }
                    }
                }

                //storeHouseNumbersObject.addInterpolationWay(mapClass.getMarker(geoPoints[0]),
                  //      geoPoints, mapClass.getMarker(geoPoints.last()))
            }
        }


    }

    // When the InfoWindow is closed, the changed housenumber and street is saved.
    override fun onClose() {
        houseNumber = houseNumberEditText.text.toString()
        street = streetNameEditText.text.toString()
        storeHouseNumbersObject.changeAddress(ID, houseNumber, street)
        mapClass.changeAddressMarker(ID, houseNumber)
        Log.i(TAG, "InfoWindow Closed.")

        // TODO : Get this to work
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(mView.applicationWindowToken, 0)
    }

}