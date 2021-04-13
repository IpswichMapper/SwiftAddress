package com.mapitall.SwiftAddress

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.jar.Manifest


enum class Side {
    LEFT,
    FORWARD,
    RIGHT
}

// currently unused GPSTracker class
// TODO : FINISH
// TODO : How to create multiple constructors instead of using null mapView?
class GPSTracker(private val context : Context,
                 private val displayLocationOverlay : Boolean = false,
                 private val mapView: MapView? = null,
                 private val minTimeForUpdates: Long = 1000)
    : LocationListener, SensorEventListener {
    private var locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
    var isGPSEnabled = false
    var isNetworkEnabled = false
    var canGetLocation = false

    private val TAG = "GPSTracker"


    private var location : Location? = null
    private var latitude : Double? = null
    private var longitude : Double? = null

    private val minDistanceForUpdates : Float = 1F // 1 meter

    private lateinit var locationManager: LocationManager

    private lateinit var sensorManager: SensorManager
    private lateinit var gravitySensor: Sensor
    private lateinit var magneticSensor: Sensor

    init {
        Log.i(TAG, "GPSTracker init")
        val locationPossible = checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        if (locationPossible) {
            Log.i(TAG, "locationPossible")
            var locationFound = findLocation()
            Log.i(TAG, "first locationFound attempt; $locationFound")
            Thread {
                while (!locationFound) {
                    Log.i(TAG, "attempting to find location again")
                    Thread.sleep(4000)
                    locationFound = findLocation()
                    Log.i(TAG, "locationFound: $locationFound")
                }
            }.start()
        }

    }

    // Gets the initial location when an object of the class is created.
    @SuppressLint("MissingPermission")
    fun findLocation() : Boolean {
        Log.i(TAG, "Attempting to find location")
        try {
            locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!isGPSEnabled) {
                Toast.makeText(
                    context, context.getString(R.string.location_not_found),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            } else {
                canGetLocation = true
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTimeForUpdates,
                    minDistanceForUpdates,
                    this
                )

                Log.i(TAG, "Location Found")
                showLocationOverlay()
                return true
            }
        } catch (e : Exception) {
            e.printStackTrace()
            return false
            // Toast.makeText(context, context.getString(R.string.location_not_found)
            //    , Toast.LENGTH_SHORT).show()
        }
    }

    private fun findCompass() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    // code that executes when the location has changed.
    override fun onLocationChanged(loc: Location) {
        location = loc
    }

    // Defining vars for bearing calculation
    private var gravity : FloatArray? = null
    private var geomagnetic : FloatArray? = null
    private var azimuth : Float? = null

    // code that executes when bearing has changed.
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values
        if (gravity != null && geomagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            val success : Boolean = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                azimuth = orientation[0] // orientation contains: azimuth (radians), pitch and roll
            }
        }
    }

    override fun onProviderDisabled(provider: String) {
        findLocation()
    }

    override fun onProviderEnabled(provider: String) {
        locationOverlay.disableMyLocation()
    }

    // Will return azimuth when called.
    fun getAzimuth() : Float? {
        return azimuth
    }

    fun getLocation() : Location? {
        return location
    }

    private fun showLocationOverlay() {
        Log.i(TAG, "Showing Location Overlay")
        // Shows current location
        if (mapView != null) {
            locationOverlay.enableMyLocation()
            mapView.overlays.add(locationOverlay)
        } else {
            Log.e(TAG, "showLocationOverlay set to true yet mapView wasn't given.")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

}
