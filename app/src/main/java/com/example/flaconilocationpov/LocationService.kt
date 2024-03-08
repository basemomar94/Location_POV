package com.example.flaconilocationpov

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings


@SuppressLint("MissingPermission")
class LocationService : Service() {
    var mLocationRequest = LocationRequest()
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    var notification: Notification? = null
    private var bluetoothScanBroadcastReceiver: BroadcastReceiver? = null
    private var TAG = this::class.java.simpleName
    private var bluetoothAdapter: BluetoothAdapter? = null
    val scanner: BluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    val scanSettings = ScanSettings.Builder().build()


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "is created")
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
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
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
        bluetoothSearch()
        initBluetoothScanBroadcastReceiver()
        mLocationRequest.priority = 100
        mLocationRequest.interval = 500
        mLocationRequest.fastestInterval = 500
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            //   sendBackgroundNotification("you are $distance away")

        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {

        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sendBackgroundNotification()
        Log.d(TAG, "is started")
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
        //      Log.d(TAG, mLastLocation!!.latitude.toString())
        //    Log.d(TAG, mLastLocation.longitude.toString())

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

    private fun sendBeaconData(rrs: Int, location: String) {
        val intent = Intent(AppConstants.DISTANCE_BROADCAST)
        intent.putExtra(AppConstants.DISTANCE, rrs)
        intent.putExtra(AppConstants.BEACON_ADDRESS, location)
        sendBroadcast(intent)
    }

    private fun sendCurrentLocationData(location: Location) {
        val intent = Intent(AppConstants.BEACON_ADDRESS)
        intent.putExtra(AppConstants.BEACON_ADDRESS, location)
        sendBroadcast(intent)
    }

    private fun bluetoothSearch() {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothAdapter?.startDiscovery()


    }


    private fun initBluetoothScanBroadcastReceiver() {
        val storedBeaconAddress = getStoredBeaconAddress()
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, scanResult: ScanResult) {
                val bluetoothDevice = scanResult.device
                bluetoothDevice?.let { device ->
                    val discoveredDevice = device.address.replace(":","")
                    Log.d(TAG,"stored mac $storedBeaconAddress  $discoveredDevice  ${scanResult.rssi}")
                    if (storedBeaconAddress == discoveredDevice) {
                        Log.d(
                            TAG,
                            "we got a matching beacon $storedBeaconAddress  ${scanResult.rssi}"
                        )
                        sendBackgroundNotification("we detect your beacon $storedBeaconAddress")
                        sendBeaconData(scanResult.rssi, bluetoothDevice.address)
                    }

                }
            }
        }
        scanner.startScan(null, scanSettings, scanCallback)
        /*
                var mFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                mFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
                mFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                mFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                bluetoothScanBroadcastReceiver = object : BroadcastReceiver() {
                    @SuppressLint("MissingPermission")
                    override fun onReceive(context: Context, intent: Intent) {
                        Log.d(TAG, "onReceive bluetoothScanBroadcastReceiver ")
                        val action = intent.action
                        val bluetoothDevice =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                        if (bluetoothDevice != null) {
                            Log.d(TAG, "got device ${bluetoothDevice.address}  $beaconAddress")
                            if (beaconAddress == bluetoothDevice.address) {
                                Log.d(TAG, "we got a matching beacon")
                                sendBackgroundNotification("we detect a beacon")
                            }
                        }
                    }
                }
                registerReceiver(bluetoothScanBroadcastReceiver, mFilter)*/
    }
}