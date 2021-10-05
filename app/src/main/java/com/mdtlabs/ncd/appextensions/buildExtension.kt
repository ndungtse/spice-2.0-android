package com.mdtlabs.ncd.appextensions

import com.mdtlabs.ncd.BuildConfig

/**
 * extension method to check the current context is debug
 * returns yes to the callback
 */
fun isDebug(callback: (yes: Boolean) -> Unit) {
    if (BuildConfig.DEBUG) {
        callback.invoke(true)
    }
}


/**
 * extension method to check the current context is release
 * return yes to the callback
 */
fun isNotDebug(callback: (yes: Boolean) -> Unit) {
    if (!BuildConfig.DEBUG) {
        callback.invoke(true)
    }
}