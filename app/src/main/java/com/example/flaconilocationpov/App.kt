package com.example.flaconilocationpov

import android.app.Application
import android.content.SharedPreferences

class App : Application() {
    companion object {
        lateinit var userPreferences: SharedPreferences

    }

    override fun onCreate() {
        super.onCreate()
        userPreferences = getSharedPreferences(AppConstants.PREF_USER_PREFERENCES, MODE_PRIVATE)

    }
}