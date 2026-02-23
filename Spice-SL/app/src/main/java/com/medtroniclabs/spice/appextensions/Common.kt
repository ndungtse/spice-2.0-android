package com.medtroniclabs.spice.appextensions

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowMetrics
import com.medtroniclabs.spice.BuildConfig

fun isDebug(callback: (yes: Boolean) -> Unit) {
    if (BuildConfig.DEBUG) {
        callback.invoke(true)
    } else {
        callback.invoke(false)
    }
}

fun getDisplayDimensions(
    activity: Activity,
    widthScale: Double,
    heightScale: Double,
): Pair<Int, Int> {
    val metrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        val width = windowMetrics.bounds.width() - insets.left - insets.right
        val height = windowMetrics.bounds.height() - insets.top - insets.bottom
        Pair(width, height)
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    val targetWidth = (metrics.first * widthScale).toInt()
    val targetHeight = (metrics.second * heightScale).toInt()

    return Pair(targetWidth, targetHeight)
}
