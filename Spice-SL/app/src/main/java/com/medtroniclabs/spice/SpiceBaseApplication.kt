package com.medtroniclabs.spice

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SpiceBaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}