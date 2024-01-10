package com.medtroniclabs.spice.appextensions

import com.medtroniclabs.spice.BuildConfig

fun isDebug(callback: (yes: Boolean) -> Unit) {
    if (BuildConfig.DEBUG) {
        callback.invoke(true)
    } else {
        callback.invoke(false)
    }
}


