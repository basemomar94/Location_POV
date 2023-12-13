package com.example.flaconilocationpov

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.datatransport.Priority
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng


class LocationService : Service() {
    var mLocationRequest = LocationRequest()
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    var notification: Notification? = null


    override fun onCreate() {
        super.onCreate()
        Log.d("SERVICE", "is created")
        startLocation()
    }

    private fun startLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFusedLocationClient?.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
        mLocationRequest.priority = 100
        mLocationRequest.interval = 500
        mLocationRequest.fastestInterval = 500
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            val distance = calculateDistance(mLastLocation)
            if (mLastLocation != null) {
                sendDistanceData(distance, mLastLocation)
            }
            sendBackgroundNotification("you are $distance away")


        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {

        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sendBackgroundNotification()
        Log.d("SERVICE", "is started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null


    private fun sendBackgroundNotification(textBody: String = "") {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val contentIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)
        notification = NotificationCompat.Builder(
            this,
            getString(R.string.default_notification_channel_id)
        )
            .setSmallIcon(androidx.core.R.drawable.notification_bg)
            .setContentTitle("Falcon POV")
            .setOnlyAlertOnce(true)
            .setOngoing(true).setContentText(textBody).setContentIntent(contentIntent)
            .setAutoCancel(false)
            .build()

        startForeground(1, notification)
    }

    private fun calculateDistance(mLastLocation: Location?): Int {
        Log.d("LOC", mLastLocation!!.latitude.toString())
        Log.d("LOC", mLastLocation.longitude.toString())
        val storedLocation = getStoredLocation()
        if (mLastLocation != null) {
            val distance = distanceBetween(
                LatLng(
                    storedLocation?.latitude ?: AppConstants.RAF_LAT.toDouble(),
                    storedLocation?.longitude ?: AppConstants.RAF_LOG.toDouble()
                ),
                LatLng(mLastLocation?.latitude ?: 0.0, mLastLocation?.longitude ?: 0.0)
            )
            return distance.toInt()
        }

        return -1

    }

    private fun distanceBetween(latLng1: LatLng, latLng2: LatLng): Float {
        try {
            val loc1 = Location(LocationManager.GPS_PROVIDER)
            loc1.latitude = latLng1.latitude
            loc1.longitude = latLng1.longitude
            val loc2 = Location(LocationManager.GPS_PROVIDER)
            loc2.latitude = latLng2.latitude
            loc2.longitude = latLng2.longitude

            // Returns the approximate distance in meters between this location and the given location.
            // Distance is defined using the WGS84 ellipsoid.
            return loc1.distanceTo(loc2)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return 0f
    }

    private fun sendDistanceData(distance: Int, location: Location) {
        val intent = Intent(AppConstants.DISTANCE_BROADCAST)
        intent.putExtra(AppConstants.DISTANCE, distance)
        intent.putExtra(AppConstants.Location, location)
        sendBroadcast(intent)
    }

    private fun sendCurrentLocationData(location: Location) {
        val intent = Intent(AppConstants.Location)
        intent.putExtra(AppConstants.Location, location)
        sendBroadcast(intent)
    }
}