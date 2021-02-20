package com.mapitall.SwiftAddress

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.contentValuesOf
import androidx.preference.PreferenceManager
import layout.AddressNodes
import layout.StoreHouseNumbers
import org.osmdroid.config.Configuration.*
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.net.IDN
import kotlin.math.max

class Map(var mapView: MapView,
          private val context: Context,
          private val mainActivity: MainActivity) {

    private var markerHashMap = HashMap<Int, Marker>()
    private var storeHouseNumbersObject = StoreHouseNumbers(context)
    private val TAG = "Map"
    init {
        // setting up map
        getInstance().load(
                context,
                PreferenceManager.getDefaultSharedPreferences(context)
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)


        // Displays all the housenumbers that have already been
        // created but haven't been stored to an OSM file yet.
        storeHouseNumbersObject.displayMarkers(this, mainActivity)

        for (marker: Marker in markerHashMap.values) {
            mapView.overlays.add(marker)
        }
    }

    fun setMarkerHashMap(HashMap : HashMap<Int, Marker>) {
        markerHashMap = HashMap
    }
    fun getMarkerHashMap() : HashMap<Int, Marker> {
        return markerHashMap
    }

    fun addHousenumberMarker(address: AddressNodes, houseNumberID : Int) {
        markerHashMap[houseNumberID] = Marker(mapView)
        markerHashMap.getValue(houseNumberID).position = GeoPoint(
                address.latitude, address.longitude)
        markerHashMap.getValue(houseNumberID).icon = ContextCompat.getDrawable(
                context, R.drawable.address)
        markerHashMap.getValue(houseNumberID).setAnchor(
                Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

        val infoWindow = MarkerWindow(
                R.layout.address_press_layout_linear,
                this,
                context,
                houseNumberID,
                mainActivity)
        //markerList.last().infoWindow = infoWindow
        markerHashMap.getValue(houseNumberID).infoWindow = infoWindow
        mapView.overlays.add(markerHashMap.getValue(houseNumberID))
        Log.i(TAG, "Housenumber Marker Added")
    }

    fun addImageMarker(imageID : Int, lat : Double, lon : Double) {
        markerHashMap[imageID] = Marker(mapView)

        markerHashMap.getValue(imageID).position = GeoPoint(lat, lon)
        markerHashMap.getValue(imageID).icon =
                ContextCompat.getDrawable(context, R.drawable.camera)
        markerHashMap.getValue(imageID).setAnchor(
                Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

        mapView.overlays.add(markerHashMap.getValue(imageID))
    }

    fun addNoteMarker(noteID : Int, lat : Double, lon : Double, noteContents : String) {
        markerHashMap[noteID] = Marker(mapView)

        markerHashMap.getValue(noteID).position = GeoPoint(lat, lon)
        markerHashMap.getValue(noteID).setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        markerHashMap.getValue(noteID).title = noteContents
        markerHashMap.getValue(noteID).icon = ContextCompat.getDrawable(
                context, R.drawable.note)
        mapView.overlays.add(markerHashMap.getValue(noteID))
    }

    fun undo() : Boolean {
        val maxID = (markerHashMap.keys).maxOrNull()
        if (maxID != null) {
            mapView.overlays.remove(markerHashMap.getValue(maxID))
            mapView.invalidate()
            markerHashMap.remove(maxID)
            return true
        } else {
            return false
        }
    }

    fun removeAt(housenumberID : Int) {
        mapView.overlays.remove(markerHashMap.getValue(housenumberID))
        markerHashMap.remove(housenumberID)
    }

    fun getMarker(id: Int): Marker {
        return markerHashMap.getValue(id)
    }

    fun removeAllMarkers() {
        for (marker: Marker in markerHashMap.values) {
            mapView.overlays.remove(marker)
            mapView.invalidate()
        }
        markerHashMap.clear()
        InfoWindow.closeAllInfoWindowsOn(mapView)
        Log.i(TAG, "Database cleared and markers have been removed.")

        Log.i(TAG, "Dialog dismissed")
    }


}