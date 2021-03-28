package com.mapitall.SwiftAddress.BackgroundImagery

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.mapitall.SwiftAddress.R
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView

class DownloadTilesFragment : Fragment(R.layout.fragment_download_tiles), MapListener {


    val TAG = "DownloadTilesFragment"

    lateinit var mapView : MapView
    lateinit var mapTooLargeTextView: TextView
    lateinit var downloadButton: Button
    lateinit var closeButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Load up map
        mapView = view.findViewById(R.id.download_tiles_map_view)
        org.osmdroid.config.Configuration.getInstance().load(
                requireContext(),
                PreferenceManager.getDefaultSharedPreferences(requireContext())
        )


        val mapboxSatellite = XYTileSource("mapbox",
            0,
            17,
            256,
            ".jpg?access_token=pk.eyJ1Ijoib3BlbnN0cmVldG1hcCIsImEiOiJja2w5YWt5bnYwNjZmMnFwZjhtbHk1MnA1In0.eq2aumBK6JuRoIuBMm6Gew",
            arrayOf(
                "https://a.tiles.mapbox.com/v4/mapbox.satellite/",
                "https://b.tiles.mapbox.com/v4/mapbox.satellite/",
                "https://c.tiles.mapbox.com/v4/mapbox.satellite/",
                "https://d.tiles.mapbox.com/v4/mapbox.satellite/")
        )

        val bingSatellite = XYTileSource("bing",
            0,
            17,
            256,
            "",
            arrayOf("https://bing.com/maps/")
        )

        val esriSatellite = object: OnlineTileSourceBase("esri",
            0,
            17,
            256,
            "",
            arrayOf(
                "https://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/",
                "https://server.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/",
            )) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                val url = baseUrl + MapTileIndex.getZoom(pMapTileIndex) +
                        "/" + MapTileIndex.getY(pMapTileIndex) +
                        "/" + MapTileIndex.getX(pMapTileIndex)
                return url
            }
        }

        // Set tile source
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val imagery = sp.getString("imagery", "Mapbox Satellite")
        when (imagery) {
            "Mapbox Satellite" -> mapView.setTileSource(mapboxSatellite)
            "Esri Satellite" -> mapView.setTileSource(esriSatellite)
        }

        // Zoom to reasonable level
        mapView.controller.setZoom(3.0)

        mapTooLargeTextView = view.findViewById(R.id.is_map_too_large)
        downloadButton = view.findViewById(R.id.download_button)
        downloadButton.setOnClickListener {
            downloadTiles()
        }
        closeButton = view.findViewById(R.id.close_download_fragment_button)
        closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        mapView.setMultiTouchControls(true)
        mapView.addMapListener(this)

    }

    private fun downloadTiles() {

        val cacheManager = CacheManager(mapView, )

        val boundingBox: BoundingBox = mapView.boundingBox
        Log.i(TAG, "boundingBox North: ${boundingBox.actualNorth}")
        Log.i(TAG, "boundingBox South: ${boundingBox.latSouth}")
        Log.i(TAG, "zoom Level: ${mapView.maxZoomLevel}")
        cacheManager.downloadAreaAsync(requireContext(), boundingBox,
                mapView.zoomLevelDouble.toInt(),
                mapView.maxZoomLevel.toInt())

        Toast.makeText(requireContext(), "Downloading Tiles", Toast.LENGTH_SHORT).show()
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return false
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        if (mapView.zoomLevelDouble < 14) {
            mapTooLargeTextView.text = getString(R.string.map_too_large)
            mapTooLargeTextView.setTextColor(Color.RED)
        } else {
            mapTooLargeTextView.text = getString(R.string.map_good_size)
            mapTooLargeTextView.setTextColor(Color.BLACK)
        }
        return true
    }
}