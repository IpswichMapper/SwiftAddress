package com.mapitall.SwiftAddress

import android.annotation.SuppressLint
import android.app.Activity
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
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.lang.invoke.MethodHandles
import java.util.jar.Manifest
import java.util.logging.Handler
import java.util.logging.LogRecord


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

    private var locationOverlay: MyLocationNewOverlay?
    var isGPSEnabled = false
    var isNetworkEnabled = false
    var canGetLocation = false

    private val TAG = "GPSTracker"


    private var location : Location? = null
    // private var latitude : Double? = null
    // private var longitude : Double? = null

    private var savedLocation : Location? = null

    private val minDistanceForUpdates : Float = 1F // 1 meter

    private lateinit var locationManager: LocationManager
    private var locationFound = false
    private lateinit var sensorManager: SensorManager
    private lateinit var gravitySensor: Sensor
    private lateinit var magneticSensor: Sensor

    init {
        if (mapView != null) {
            locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        } else {
            locationOverlay = null
            // Only ClassicMainActivity has no mapView, and only it needs compass
            // So function runs when no mapView is given
            findCompass()
        }
        Log.i(TAG, "GPSTracker init")
        val locationPossible = checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        if (locationPossible) {
            Log.i(TAG, "locationPossible")
            locationFound = findLocation()
            Log.i(TAG, "first locationFound attempt; locationFound = $locationFound")
            Thread {
                // Make it true so the while loop runs at least once
                isGPSEnabled = true
                while (!locationFound && isGPSEnabled) {
                    Log.i(TAG, "attempting to find location again")
                    Thread.sleep(4000)
                    val looper = Looper.getMainLooper()
                    (context as Activity).runOnUiThread {
                        locationFound = findLocation()
                    }
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
                /*
                Toast.makeText(
                    context, context.getString(R.string.location_not_found),
                    Toast.LENGTH_SHORT
                ).show()
                 */
                Log.w(TAG, "Location not found")
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
                if (mapView != null) {
                    showLocationOverlay()
                }
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
        locationFound = false
        locationOverlay?.disableMyLocation()
    }

    override fun onProviderEnabled(provider: String) {
        locationFound = false
        Log.i(TAG, "Location provider enabled")
        Thread {
            // Make it true so the while loop runs at least once

            while (!locationFound && isGPSEnabled) {
                Log.i(TAG, "attempting to find location again")
                Thread.sleep(4000)
                (context as Activity).runOnUiThread {
                    locationFound = findLocation()
                }
                Log.i(TAG, "locationFound: $locationFound")
            }
            if (locationFound) locationOverlay?.enableMyLocation()
        }.start()
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
            locationOverlay?.enableMyLocation()
            mapView.overlays.add(locationOverlay)
        } else {
            Log.e(TAG, "showLocationOverlay called yet mapView wasn't given.")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    fun saveLocation() {
        savedLocation = getLocation()
    }

    fun getSavedLocation() : Location? {
        return savedLocation
    }

}
