package com.mapitall.SwiftAddress.BackgroundImagery

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.mapitall.SwiftAddress.R
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
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import de.westnordost.osmapi.map.data.BoundingBox as OverpassBoundingBox

class DownloadTilesFragment : Fragment(R.layout.fragment_download_tiles), MapListener {


    private val downloadedMarkersList = mutableListOf<Marker>()
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

        private fun downloadHousenumberMarkers() {

            val downloadedHousenumbersHandler = object: MapDataWithGeometryHandler {

                override fun handle(bounds: OverpassBoundingBox) {
                    Log.i(TAG, "boundingBox: $bounds")
                }

                override fun handle(node: Node) {
                    requireActivity().runOnUiThread {
                        val marker = Marker(mapView)
                        marker.icon = ContextCompat.getDrawable(requireContext(),
                            R.drawable.address_downloaded)
                        marker.position = GeoPoint(node.position.latitude, node.position.longitude)
                        marker.title = node.tags.getValue("addr:housenumber")
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        downloadedMarkersList.add(marker)
                        mapView.overlays.add(downloadedMarkersList.last())
                        mapView.invalidate()
                    }
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
                        marker.title = way.tags.getValue("addr:housenumber")
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        downloadedMarkersList.add(marker)
                        mapView.overlays.add(downloadedMarkersList.last())
                        mapView.invalidate()
                    }
                }

                override fun handle(
                    relation: Relation,
                    bounds: OverpassBoundingBox,
                    nodeGeometries: MutableMap<Long, LatLon>,
                    wayGeometries: MutableMap<Long, MutableList<LatLon>>
                ) {
                    Log.i(TAG, "lmao")
                }
            }

            val connection = OsmConnection("https://overpass-api.de/api/", null)
            val overpassApi = OverpassMapDataDao(connection)

            val boundingBox = mapView.boundingBox
            val query = "[bbox:${boundingBox.latSouth}, ${boundingBox.lonWest}, " +
                    "${boundingBox.latNorth}, ${boundingBox.lonEast}]; nwr['addr:housenumber'];" +
                    "out meta geom;"

            Thread {
                overpassApi.queryElementsWithGeometry(query, downloadedHousenumbersHandler)
            }.start()

        }
}