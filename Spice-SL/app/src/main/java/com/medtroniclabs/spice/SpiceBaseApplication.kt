package com.medtroniclabs.spice

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.medtroniclabs.spice.appextensions.isDebug
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.log.CrashReportingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class SpiceBaseApplication: Application(), Configuration.Provider {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        initTimber()
        initPreference()
    }


    /**
     * method to initialize preference
     */
    private fun initPreference() {
        SecuredPreference
            .build(packageName, applicationContext)
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

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(hiltWorkerFactory).build()
}

