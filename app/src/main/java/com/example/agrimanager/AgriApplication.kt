package com.example.agrimanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.google.firebase.FirebaseApp

@HiltAndroidApp
class AgriApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Force initialization
        FirebaseApp.initializeApp(this)
    }
}