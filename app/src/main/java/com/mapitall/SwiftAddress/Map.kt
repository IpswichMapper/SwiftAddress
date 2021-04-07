package com.mapitall.SwiftAddress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class Map(var mapView: MapView,
          private val context: Context,
          private val mainActivity: MainActivity) {

    private var markerHashMap = HashMap<Int, Marker>()
    private var polyLineID : Int
    private var polyLineHashMap = HashMap<Int, Polyline>()
    private var storeHouseNumbersObject = StoreHouseNumbers(context)
    private val TAG = "Map"
    private var moveMarkerMapListener: MapListener
    lateinit var makeInterpolationWayFollowCenterMapListener: MapListener
    var moveMarkerCondition = false
    lateinit var markerToMove : Marker
    private var textMarkersPresent = false

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

        // Get ID of last added polyline
        val lastID = storeHouseNumbersObject.lastPolyLineID()
        polyLineID =
                if (lastID != -1) lastID + 1
                else 1

        moveMarkerMapListener = object:MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                moveMarker()
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                return false
            }
        }

        mapView.addMapListener(moveMarkerMapListener)

    }

    fun setMarkerHashMap(HashMap : HashMap<Int, Marker>) {
        for (marker: Marker in markerHashMap.values) {
            mapView.overlays.remove(marker)
        }

        markerHashMap = HashMap
        for (marker: Marker in markerHashMap.values) {
            mapView.overlays.add(marker)
        }
    }

    /*
    fun getMarkerHashMap() : HashMap<Int, Marker> {
        return markerHashMap
    }
     */

    // adds a housenumber marker to the map
    fun addHousenumberMarker(address: AddressNodes, houseNumberID : Int) {
        markerHashMap[houseNumberID] = Marker(mapView)
        markerHashMap.getValue(houseNumberID).position = GeoPoint(
                address.latitude, address.longitude)

        val drawable = ContextCompat.getDrawable(context, R.drawable.address)!!
        val housenumber = storeHouseNumbersObject.getHouseNumber(houseNumberID)
        if (housenumber.length <= 5) {

            val bitmap = drawable.toBitmap()

            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            if (housenumber.length <= 3) {
                paint.textSize = 40f
            } else if (housenumber.length == 4) {
                paint.textSize = 30f
            } else {
                paint.textSize = 25f
            }
            paint.textAlign = Paint.Align.CENTER

            val canvas = Canvas(bitmap)

            // https://stackoverflow.com/a/11121873
            // explanation of this line
            canvas.drawText(housenumber, bitmap.width / 2f,
                    bitmap.height / 2f - (paint.descent() + paint.ascent() / 2), paint)

            val icon = BitmapDrawable(context.resources, bitmap)
            markerHashMap.getValue(houseNumberID).icon = icon
        } else {
            markerHashMap.getValue(houseNumberID).icon = drawable
        }
        markerHashMap.getValue(houseNumberID).setAnchor(
                Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

        val infoWindow = AddressMarkerWindow(
                R.layout.address_press_layout_linear,
                this,
                context,
                houseNumberID,
                mainActivity,
                address.housenumber,
                address.street)
        //markerList.last().infoWindow = infoWindow
        markerHashMap.getValue(houseNumberID).infoWindow = infoWindow
        mapView.overlays.add(markerHashMap.getValue(houseNumberID))
        Log.i(TAG, "Housenumber Marker Added")
    }

    // adds an image marker to the map
    fun addImageMarker(imageID : Int, lat : Double, lon : Double) {
        markerHashMap[imageID] = Marker(mapView)

        markerHashMap.getValue(imageID).position = GeoPoint(lat, lon)
        markerHashMap.getValue(imageID).icon =
                ContextCompat.getDrawable(context, R.drawable.camera)
        markerHashMap.getValue(imageID).setAnchor(
                Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

        mapView.overlays.add(markerHashMap.getValue(imageID))
    }




    // adds a note marker to the map
    fun addNoteMarker(noteID : Int, lat : Double, lon : Double, noteContents : String) {
        markerHashMap[noteID] = Marker(mapView)

        markerHashMap.getValue(noteID).position = GeoPoint(lat, lon)
        markerHashMap.getValue(noteID).setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        markerHashMap.getValue(noteID).icon = ContextCompat.getDrawable(
                context, R.drawable.note)
        val infoWindow = NoteMarkerWindow(
            this,
            context,
            noteID,
            mainActivity,
            noteContents)
        markerHashMap.getValue(noteID).infoWindow = infoWindow
        mapView.overlays.add(markerHashMap.getValue(noteID))
    }

    // removes the last added marker from the map
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

    // removes a marker at a specific ID.
    fun removeAt(ID : Int) {
        mapView.overlays.remove(markerHashMap.getValue(ID))
        markerHashMap.remove(ID)
    }

    // Returns a marker after being given the marker ID.
    fun getMarker(id: Int): Marker {
        return markerHashMap.getValue(id)
    }

    // Returns a marker after being given the exact co-ordinates of the marker
    fun getMarker(geoPoint: GeoPoint): Marker? {
        for (marker in markerHashMap.values) {
            if (marker.position as GeoPoint == geoPoint) {
                return marker
            }
        }
        return null
    }

    // removes all markers from the map
    fun removeAllMarkers() {
        mapView.overlays.removeAll(mapView.overlays)
        mapView.invalidate()
    }

    // fixes a marker into a new position after the "move" button in the mainActivity is pressed.
    fun moveMarker() {
        if (moveMarkerCondition) {
            markerToMove.position = mapView.mapCenter as GeoPoint
            mapView.invalidate()
        }
    }

    // Creates a new "interpolation" way, this effectively allows you to guess the addresses
    // between two existing addresses.
    fun createNewInterpolationWay(startMarkerID: Int) {
        val marker = getMarker(startMarkerID)
        val geoPoints = arrayListOf(marker.position as GeoPoint)
        polyLineHashMap[polyLineID] =
                Polyline(null, true)

        polyLineHashMap.getValue(polyLineID).setPoints(geoPoints)
        polyLineHashMap.getValue(polyLineID).outlinePaint.color = ContextCompat.getColor(
                context, R.color.interpolation_way_color)
        polyLineHashMap.getValue(polyLineID).outlinePaint.style = Paint.Style.STROKE
        polyLineHashMap.getValue(polyLineID).outlinePaint.pathEffect = (
                DashPathEffect(floatArrayOf(50f, 10f), 100f))

        // polyLineHashMap.getValue(polyLineID).addPoint(marker.position)

        mapView.overlays.add(polyLineHashMap.getValue(polyLineID))
        makeLineFollowCenter(true)

        mainActivity.createNewInterpolationWay(
                polyLineHashMap.getValue(polyLineID), geoPoints, startMarkerID)

        mapView.mapCenter as GeoPoint
        mapView.context

    }

    fun addPolyLineToHashMap(polyline: Polyline, polyLineID : Int) {
        polyLineHashMap[polyLineID] = polyline

        polyLineHashMap.getValue(polyLineID).outlinePaint.color = ContextCompat.getColor(
                context, R.color.interpolation_way_color)
        polyLineHashMap.getValue(polyLineID).outlinePaint.style = Paint.Style.STROKE
        polyLineHashMap.getValue(polyLineID).outlinePaint.pathEffect = (
                DashPathEffect(floatArrayOf(50f, 10f), 100f))
    }
    fun finishInterpolationWay(endMarkerID: Int) {

        makeLineFollowCenter(false)
        polyLineHashMap.getValue(polyLineID).addPoint(getMarker(endMarkerID).position)
        polyLineID += 1
    }

    // makes the interpolation line follow the map center
    fun makeLineFollowCenter(follow : Boolean, geoPoints: ArrayList<GeoPoint>) {

        val line = polyLineHashMap.getValue(polyLineID)
        if (follow) {
            makeInterpolationWayFollowCenterMapListener = object:MapListener {
                override fun onZoom(event: ZoomEvent?): Boolean {
                    return false
                }

                override fun onScroll(event: ScrollEvent?): Boolean {
                    geoPoints.removeLast()
                    geoPoints.add(mapView.mapCenter as GeoPoint)
                    line.setPoints(geoPoints)
                    return true
                }
            }
            geoPoints.add(mapView.mapCenter as GeoPoint)
            mapView.addMapListener(makeInterpolationWayFollowCenterMapListener)
        } else {
            mapView.removeMapListener(makeInterpolationWayFollowCenterMapListener)
        }
    }
    fun makeLineFollowCenter(follow : Boolean) {

        val line = polyLineHashMap.getValue(polyLineID)
        val geoPoints = polyLineHashMap.getValue(polyLineID).actualPoints.toMutableList()
        if (follow) {
            makeInterpolationWayFollowCenterMapListener = object:MapListener {
                override fun onZoom(event: ZoomEvent?): Boolean {
                    return false
                }

                override fun onScroll(event: ScrollEvent?): Boolean {
                    geoPoints.removeLast()
                    geoPoints.add(mapView.mapCenter as GeoPoint)
                    line.setPoints(geoPoints)
                    return true
                }
            }
            geoPoints.add(mapView.mapCenter as GeoPoint)
            mapView.addMapListener(makeInterpolationWayFollowCenterMapListener)
        } else {
            mapView.removeMapListener(makeInterpolationWayFollowCenterMapListener)
        }
    }

    fun getPolyLineHashMap() : HashMap<Int, Polyline> {
        return polyLineHashMap
    }
    fun setPolylineHashMap(polyLineHashMap_: HashMap<Int, Polyline>) {
        for (polyline in polyLineHashMap.values) {
            mapView.overlays.remove(polyline)
        }
        for (polyline in polyLineHashMap_) {
            for(point in polyline.value.actualPoints) {
                Log.i(TAG, "point: $point")
            }
            addPolyLineToHashMap(polyline.value, polyline.key)
        }
        for (polyline in polyLineHashMap.values) {
            mapView.overlays.add(polyline)
        }
    }
    // Gets a specific polyline when given ID.
    private fun getPolyLineHashMapValue(ID: Int) : Polyline {
        return  polyLineHashMap.getValue(ID)
    }
    // get a list of points for a specific Polyline
    fun getPolyLineHashMapValue(ID: Int, getPoints: Boolean) : MutableList<GeoPoint> {
        return polyLineHashMap.getValue(ID).actualPoints.toMutableList()
    }
    fun getPolyLineID() : Int {
        return polyLineID
    }

    fun changeAddressMarker(id: Int, houseNumber: String) {
        val marker = getMarker(id)
        mapView.overlays.remove(marker)

        val drawable = ContextCompat.getDrawable(context, R.drawable.address)!!
        if (houseNumber.length <= 5) {
            val bitmap = drawable.toBitmap()

            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            if (houseNumber.length <= 3) {
                paint.textSize = 40f
            } else if (houseNumber.length == 4) {
                paint.textSize = 30f
            } else {
                paint.textSize = 25f
            }
            paint.textAlign = Paint.Align.CENTER

            val canvas = Canvas(bitmap)

            // https://stackoverflow.com/a/11121873
            // explanation of this line
            canvas.drawText(houseNumber, bitmap.width / 2f,
                    bitmap.height / 2f - (paint.descent() + paint.ascent() / 2), paint)

            val icon = BitmapDrawable(context.resources, bitmap)

            marker.icon = icon
        } else {
            marker.icon = drawable
        }
        mapView.overlays.add(marker)
    }


}