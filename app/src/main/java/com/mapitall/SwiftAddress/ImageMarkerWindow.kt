package com.mapitall.SwiftAddress

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.infowindow.InfoWindow

class ImageMarkerWindow(private val mapClass: Map,
                        private val context: Context,
                        private val ID: Long,
                        private val mainActivity: MainActivity) :
        InfoWindow(R.layout.image_press_layout_linear, mapClass.mapView) {

    private lateinit var storeHouseNumbersObject: StoreHouseNumbers

    private val TAG = "PolylineMarkerWindow"

    override fun onOpen(item: Any?) {

        storeHouseNumbersObject = StoreHouseNumbers(context)
        val moveButton = mView.findViewById<Button>(R.id.move_image_linear)
        val deleteButton = mView.findViewById<Button>(R.id.delete_image_linear)

        moveButton.setOnClickListener {
            val marker = mapClass.getMarker(ID)
            marker.position = mapClass.mapView.mapCenter as GeoPoint
            mainActivity.moveMarker(ID, marker, false)
            close()
        }
        deleteButton.setOnClickListener {
            mapClass.removeAt(ID)
            close()
        }

        val imageView = mView.findViewById<ImageView>(R.id.image_linear)
        val imagePath = storeHouseNumbersObject.getImageFilePath(ID)

        imageView.setImageDrawable(Drawable.createFromPath(imagePath))
    }

    override fun onClose() {
        try {
            mapClass.getMarker(ID) // Simply to active catch block if marker is deleted
        } catch (e: NoSuchElementException) {
            Log.i(TAG, "Marker was deleted")
        } finally {
            Log.i(TAG, "InfoWindow Closed.")
        }
        // TODO: Add ability to hide keyboard
    }

}