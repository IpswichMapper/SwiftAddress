package com.mapitall.SwiftAddress

import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.Toast
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

class MarkerWindow(pressLayoutId : Int, mapView: MapView, context_ : Context) :
        InfoWindow(pressLayoutId, mapView) {

    private val TAG = "MarkerWindow"
    private val context = context_
    override fun onOpen(item: Any?) {
        closeAllInfoWindowsOn(mapView)
        Log.i(TAG, "InfoWindow Opened.")

        mView.setOnClickListener {
            close()
        }

        val moveButton = mView.findViewById<Button>(R.id.Move_linear)
        moveButton.setOnClickListener {
            Toast.makeText(context, R.string.unimplemented, Toast.LENGTH_SHORT).show()
        }

        val interpolateButton = mView.findViewById<Button>(R.id.Interpolate_linear)
        interpolateButton.setOnClickListener {
            Toast.makeText(context, R.string.unimplemented, Toast.LENGTH_SHORT).show()
        }

        val deleteButton = mView.findViewById<Button>(R.id.Delete_linear)
        deleteButton.setOnClickListener {
            Toast.makeText(context, R.string.unimplemented, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClose() {
        Log.i(TAG, "InfoWindow Closed.")
    }

}