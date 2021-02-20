package com.mapitall.SwiftAddress

import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.Toast
import layout.StoreHouseNumbers
import org.osmdroid.views.overlay.infowindow.InfoWindow

class MarkerWindow(pressLayoutId : Int,
                   private val mapClass: Map,
                   private val context: Context,
                   private val ID : Int,
                   private val mainActivity: MainActivity) :
        InfoWindow(pressLayoutId, mapClass.mapView) {

    private val TAG = "MarkerWindow"
    private val storeHouseNumbersObject = StoreHouseNumbers(context)
    override fun onOpen(item: Any?) {
        closeAllInfoWindowsOn(mapView)
        Log.i(TAG, "InfoWindow Opened.")

        mView.setOnClickListener {
            close()
        }

        val moveButton = mView.findViewById<Button>(R.id.Move_linear)
        moveButton.setOnClickListener {
            // Toast.makeText(context, R.string.unimplemented, Toast.LENGTH_SHORT).show()
            mainActivity.moveMarker(ID, mapClass.getMarker(ID))
            close()
        }

        val interpolateButton = mView.findViewById<Button>(R.id.Interpolate_linear)
        interpolateButton.setOnClickListener {
            Toast.makeText(context, R.string.unimplemented, Toast.LENGTH_SHORT).show()
        }

        val deleteButton = mView.findViewById<Button>(R.id.Delete_linear)
        deleteButton.setOnClickListener {
            // Toast.makeText(context, R.string.unimplemented, Toast.LENGTH_SHORT).show()
            storeHouseNumbersObject.removeAt(ID)
            mapClass.removeAt(ID)
            close()
        }
    }

    override fun onClose() {
        Log.i(TAG, "InfoWindow Closed.")
    }

}