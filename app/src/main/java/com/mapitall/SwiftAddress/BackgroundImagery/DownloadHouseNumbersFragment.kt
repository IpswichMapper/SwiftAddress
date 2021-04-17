package com.mapitall.SwiftAddress.BackgroundImagery

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.mapitall.SwiftAddress.*
import com.mapitall.SwiftAddress.Map
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import de.westnordost.osmapi.overpass.MapDataWithGeometryHandler
import de.westnordost.osmapi.overpass.OverpassMapDataDao
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import de.westnordost.osmapi.map.data.BoundingBox as OverpassBoundingBox

class DownloadHouseNumbersFragment : Fragment(R.layout.fragment_download_tiles), MapListener {


    private val downloadedMarkersList = mutableListOf<Marker>()
    val TAG = "DownloadTilesFragment"

    lateinit var mapView: MapView
    private lateinit var mapTooLargeTextView: TextView
    lateinit var downloadHousenumbersButton: Button
    lateinit var closeButton: Button
    private lateinit var storeHouseNumbersObject: StoreHouseNumbers
    private lateinit var button: Button

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
            20,
            256,
            "",
            arrayOf("https://bing.com/maps/")
        )

        val esriSatellite = object: OnlineTileSourceBase("esri",
            0,
            20,
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
        when (sp.getString("imagery", "Osm Carto")) {
            "Osm Carto" -> mapView.setTileSource(TileSourceFactory.MAPNIK)
            "Mapbox Satellite" -> mapView.setTileSource(mapboxSatellite)
            "Esri Satellite" -> mapView.setTileSource(esriSatellite)
            else -> {
                mapView.setTileSource(TileSourceFactory.MAPNIK)
            }
        }

        // Zoom to reasonable level
        mapView.controller.setZoom(3.0)

        mapTooLargeTextView = view.findViewById(R.id.is_map_too_large)
        downloadHousenumbersButton = view.findViewById(R.id.download_button)
        downloadHousenumbersButton.setOnClickListener {
            downloadHousenumberMarkers()
        }
        closeButton = view.findViewById(R.id.close_download_fragment_button)
        closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        button = requireView().findViewById(R.id.download_button)
        button.alpha = 0.5f
        button.isEnabled = false

        val title = requireView().findViewById<TextView>(R.id.download_tiles_text_view)
        title.text = getString(R.string.download_house_numbers)

        mapView.setMultiTouchControls(true)
        mapView.addMapListener(this)

        @Suppress("UNCHECKED_CAST")
        Log.w(TAG, "arguments: $arguments?")

        storeHouseNumbersObject = StoreHouseNumbers(requireContext())

        storeHouseNumbersObject.displayDownloadedMarkers(mapView)

    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return false
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        if (mapView.zoomLevelDouble < 17) {
            mapTooLargeTextView.text = getString(R.string.map_too_large)
            mapTooLargeTextView.setTextColor(Color.RED)
            button.alpha = 0.5f
            button.isEnabled = false

        } else {
            mapTooLargeTextView.text = getString(R.string.map_good_size)
            mapTooLargeTextView.setTextColor(ContextCompat.getColor(
                    requireContext(), R.color.button_colors))
            button.alpha = 1f
            button.isEnabled = true
        }
        return true
    }

    private fun downloadHousenumberMarkers() {

        val downloadingDialog = AlertDialog.Builder(requireContext())
        downloadingDialog.setTitle(getString(R.string.downloading_addresses))
        downloadingDialog.setMessage(getString(R.string.please_wait))
        downloadingDialog.setCancelable(false)
        val dialog = downloadingDialog.create()

        val downloadedHousenumbersHandler = object: MapDataWithGeometryHandler {

            override fun handle(bounds: OverpassBoundingBox) {
                dialog.dismiss()
            }

            override fun handle(node: Node) {
                val marker = Marker(mapView)
                marker.icon = ContextCompat.getDrawable(requireContext(),
                        R.drawable.address_downloaded)
                marker.position = GeoPoint(node.position.latitude, node.position.longitude)
                try {
                    marker.title = node.tags["addr:housenumber"]
                } catch (e: NoSuchElementException) {
                    e.printStackTrace()
                    marker.title = node.tags["addr:housename"]
                }
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                downloadedMarkersList.add(marker)
                mapView.overlays.add(downloadedMarkersList.last())
                mapView.invalidate()

                storeHouseNumbersObject.addDownloadedHousenumber(node)
            }

            override fun handle(way: Way,
                                bounds: OverpassBoundingBox,
                                geometry: MutableList<LatLon>) {

                // https://stackoverflow.com/a/14231286
                var x = 0.0
                var y = 0.0
                var z = 0.0

                for (point in geometry) {
                    val latAngle = point.latitude * (Math.PI / 180)
                    val lonAngle = point.longitude * (Math.PI / 180)

                    x += cos(latAngle) * cos(lonAngle)
                    y += cos(latAngle) * kotlin.math.sin(lonAngle)
                    z += sin(latAngle)
                }

                x /= geometry.size
                y /= geometry.size
                z /= geometry.size

                val centralLongitude = atan2(y, x)
                val centralSquareRoot = sqrt((x * x) + (y * y))
                val centralLatitude = atan2(z, centralSquareRoot)

                val trueLat = centralLatitude * (180 / Math.PI)
                val trueLon = centralLongitude * (180 / Math.PI)

                requireActivity().runOnUiThread {
                    val marker = Marker(mapView)
                    marker.icon = ContextCompat.getDrawable(requireContext(),
                        R.drawable.address_downloaded)
                    marker.position = GeoPoint(trueLat, trueLon)
                    try {
                        marker.title = way.tags["addr:housenumber"]
                    } catch (e: NoSuchElementException) {
                        e.printStackTrace()
                        marker.title = way.tags["addr:housename"]
                    }
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    downloadedMarkersList.add(marker)
                    mapView.overlays.add(downloadedMarkersList.last())
                    mapView.invalidate()
                }

                storeHouseNumbersObject.addDownloadedHousenumber(way, trueLat, trueLon)
            }

            override fun handle(
                relation: Relation,
                bounds: OverpassBoundingBox,
                nodeGeometries: MutableMap<Long, LatLon>,
                wayGeometries: MutableMap<Long, MutableList<LatLon>>
            ) {

            }
        }

        val connection = OsmConnection("https://overpass-api.de/api/", null)
        val overpassApi = OverpassMapDataDao(connection)

        val boundingBox = mapView.boundingBox
        val query = "[bbox:${boundingBox.latSouth}, ${boundingBox.lonWest}, " +
                "${boundingBox.latNorth}, ${boundingBox.lonEast}]; " +
                "(" +
                "nwr['addr:housenumber'];" +
                "nwr['addr:housename'];" +
                ");" +
                "out meta geom;"
        dialog.show()

        Thread {
            overpassApi.queryElementsWithGeometry(query, downloadedHousenumbersHandler)
        }.start()

    }
}