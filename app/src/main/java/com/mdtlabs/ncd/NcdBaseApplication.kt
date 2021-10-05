package com.mdtlabs.ncd

import android.app.Application
import android.content.ContextWrapper
import com.mdtlabs.ncd.appextensions.isDebug
import com.mdtlabs.ncd.appextensions.isNotDebug
import com.mdtlabs.ncd.common.TranslateLanguage
import com.mdtlabs.ncd.log.CrashReportingTree
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class NcdBaseApplication : Application() {

    @Inject
    lateinit var translateLanguage: TranslateLanguage


    override fun onCreate() {
        super.onCreate()
        initTimber()
        initPreference()
    }

    /**
     * method to print debug and release logs
     */
    private fun initTimber() {
        isDebug {
            Timber.plant(Timber.DebugTree())
        }
        isNotDebug {
            Timber.plant(CrashReportingTree())
        }
    }

    /**
     * method to initialize preference
     */
    private fun initPreference() {
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
    }



}