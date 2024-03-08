package com.example.flaconilocationpov

import android.location.Location
import android.location.LocationManager

fun storeBeaconAddress(macAddress: String) {
    App.userPreferences.edit().putString(AppConstants.SAVED_LAT, macAddress)
        .apply()
}

fun getStoredBeaconAddress() = App.userPreferences.getString(AppConstants.SAVED_LAT, "BC572902A0CC")
