package com.example.flaconilocationpov

import android.location.Location
import android.location.LocationManager

fun storeLocation(location: Location) {
    App.userPreferences.edit().putString(AppConstants.SAVED_LAT, location.latitude.toString())
        .apply()
    App.userPreferences.edit().putString(AppConstants.SAVED_LOG, location.longitude.toString())
        .apply()

}

fun getStoredLocation(): Location? {
    val lat = App.userPreferences.getString(AppConstants.SAVED_LAT, AppConstants.RAF_LAT)
        ?.toDoubleOrNull()
    val log = App.userPreferences.getString(AppConstants.SAVED_LOG, AppConstants.RAF_LOG)
        ?.toDoubleOrNull()
    val location = Location(LocationManager.GPS_PROVIDER)
    if (lat != null) {
        location.latitude = lat
    }
    if (log != null) {
        location.longitude = log
    }

    return if (lat != null && log != null) location else null

}
