package com.medtroniclabs.spice

import android.app.Application
import com.medtroniclabs.spice.appextensions.isDebug
import com.medtroniclabs.spice.log.CrashReportingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SpiceBaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        initTimber()
    }
}


/**
 * method to print debug and release logs
 */
private fun initTimber() {
    isDebug { debug ->
        if (debug)
            Timber.plant(Timber.DebugTree())
        else
            Timber.plant(CrashReportingTree())
    }
}