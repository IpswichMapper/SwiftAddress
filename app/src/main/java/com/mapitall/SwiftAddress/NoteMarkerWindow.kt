package com.mapitall.SwiftAddress

import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.EditText
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.infowindow.InfoWindow

class NoteMarkerWindow(private val mapClass: Map,
                       private val context: Context,
                       private val ID: Int,
                       private val mainActivity: MainActivity,
                       private var note: String) :
    InfoWindow(R.layout.note_press_layout_linear, mapClass.mapView) {

    val TAG = "NoteMarkerWindow"

    val storeHouseNumbersObject = StoreHouseNumbers(context)
    val noteEditText = mView.findViewById<EditText>(R.id.note_edit_text)

    override fun onOpen(item: Any?) {
        if (note != "") noteEditText.setText(note)

        Log.i(TAG, "Note InfoWindow Opened")

        mView.setOnClickListener {
            close()
        }

        val moveButton = mView.findViewById<Button>(R.id.move_note_linear)
        val deleteButton = mView.findViewById<Button>(R.id.delete_note_linear)

        moveButton.setOnClickListener {
            val marker = mapClass.getMarker(ID)
            marker.position = mapClass.mapView.mapCenter as GeoPoint
            mainActivity.moveMarker(ID, marker)
            close()
        }
        deleteButton.setOnClickListener {
            mapClass.removeAt(ID)
            close()
        }

    }

    override fun onClose() {
        try {
            mapClass.getMarker(ID) // Simply to active catch block if marker is deleted
            note = noteEditText.text.toString()
            storeHouseNumbersObject.changeNote(ID, note)
        } catch (e: NoSuchElementException) {
            Log.i(TAG, "Marker was deleted")
        } finally {
            Log.i(TAG, "InfoWindow Closed.")
        }
        // TODO: Add ability to hide keyboard
    }


}