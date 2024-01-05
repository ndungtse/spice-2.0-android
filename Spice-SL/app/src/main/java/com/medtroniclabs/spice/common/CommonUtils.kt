package com.medtroniclabs.spice.common

import android.content.Context
import com.medtroniclabs.spice.R

object CommonUtils {
    fun checkIsTablet(context: Context): Boolean {
        val res = context.resources?.getBoolean(R.bool.isTablet)
        return res ?: false
    }
}