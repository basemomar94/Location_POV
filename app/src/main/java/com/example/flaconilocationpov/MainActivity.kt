package com.example.flaconilocationpov

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.flaconilocationpov.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var currentBeaconAddress: String? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            Log.d("broadcastReceiver", "$intent")

            val distance = intent?.getIntExtra(AppConstants.DISTANCE, -1)
            Log.d("broadcastReceiver", "distance is $distance")
            binding.text.text = distance.toString()

            currentBeaconAddress = intent?.getStringExtra(AppConstants.BEACON_ADDRESS)
            binding.current.text = "$currentBeaconAddress is detected"
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionCheck()
        val intentFilter = IntentFilter(AppConstants.DISTANCE_BROADCAST).also {
            it.addAction(AppConstants.LOCATION_BROADCAST)
        }
        registerReceiver(broadcastReceiver, intentFilter)
        binding.confirmHome.setOnClickListener {
            storeBeaconAddress(binding.home.text.toString())
            binding.current.text = ""
        }
        getStoredBeaconAddress()?.let { address ->
            binding.home.setText(address)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        createNotificationChannel()
    }

    private fun permissionCheck() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPerms()

        } else {
            createNotificationChannel()

        }
    }

    private fun requestPerms() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH
                ),
                100
            )
        }
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