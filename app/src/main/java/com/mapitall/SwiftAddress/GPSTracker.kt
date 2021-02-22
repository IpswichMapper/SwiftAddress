package com.mapitall.SwiftAddress

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Toast


enum class Side {
    LEFT,
    FORWARD,
    RIGHT
}

// currently unused GPSTracker class
// TODO : FINISH
class GPSTracker(private val context : Context, displayLocationOverlay : Boolean = false)
    : LocationListener, SensorEventListener {
    var isGPSEnabled = false
    var isNetworkEnabled = false
    var canGetLocation = false

    private val TAG = "GPSTracker"


    private var location : Location? = null
    private var latitude : Double? = null
    private var longitude : Double? = null

    private val minDistanceForUpdates : Float = 1F // 1 meter
    private val minTimeForUpdates : Long = 1000 // 1000 milliseconds

    private lateinit var locationManager: LocationManager

    init {
        getLocation()
    }

    @SuppressLint("MissingPermission")
    fun getLocation() {
        try {
            locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!isGPSEnabled) {
                Toast.makeText(
                    context, context.getString(R.string.location_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                canGetLocation = true

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTimeForUpdates,
                    minDistanceForUpdates,
                    this
                )


            }
        } catch (e : Exception) {
            e.printStackTrace()
            // Toast.makeText(context, context.getString(R.string.location_not_found)
            //    , Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLocationChanged(location: Location) {
    }

    private var gravity : FloatArray? = null
    private var geomagnetic : FloatArray? = null
    var azimuth : Float? = null

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values;
        if (gravity != null && geomagnetic != null) {
            var R = FloatArray(9)
            var I = FloatArray(9)
            val success : Boolean = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0]; // orientation contains: azimuth, pitch and roll
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


}
