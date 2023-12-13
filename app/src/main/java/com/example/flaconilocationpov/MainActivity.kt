package com.example.flaconilocationpov

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.util.Log
import com.example.flaconilocationpov.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var currentLocation: Location? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            Log.d("broadcastReceiver", "$intent")

            val distance = intent?.getIntExtra(AppConstants.DISTANCE, -1)
            Log.d("broadcastReceiver", "distance is $distance")
            if (distance != null) {
                binding.text.text = "you are $distance away"
                getStoredLocation()?.let { location ->
                    binding.home.text = "home is ${location.latitude}   ${location.longitude}"

                }

                binding.result.text = if (distance < 50) "You are home" else "You are away"


            }
            currentLocation = intent?.getParcelableExtra(AppConstants.Location)
            binding.current.text =
                "current location is ${currentLocation?.latitude}  ${currentLocation?.longitude}"
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPerms()
        createNotificationChannel()
        val intentFilter = IntentFilter(AppConstants.DISTANCE_BROADCAST).also {
            it.addAction(AppConstants.LOCATION_BROADCAST)
        }
        registerReceiver(broadcastReceiver, intentFilter)
        binding.confirmHome.setOnClickListener {
            currentLocation?.let { it1 -> storeLocation(it1) }
        //    throw RuntimeException("Test Crash") // Force a crash
        }
    }

    private fun requestPerms() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            ),
            100
        )
    }

    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            getString(R.string.default_notification_channel_id),
            AppConstants.NOTIFICATION_CHANNEL_NAME, importance
        )
        channel.enableVibration(true)

        channel.description = AppConstants.NOTIFICATION_CHANNEL_DESCRIPTION
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)

        startLocationService()
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        startService(intent)
    }


}