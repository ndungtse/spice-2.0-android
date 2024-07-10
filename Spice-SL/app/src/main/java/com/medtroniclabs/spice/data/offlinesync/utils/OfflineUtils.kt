package com.medtroniclabs.spice.data.offlinesync.utils

import com.medtroniclabs.spice.BuildConfig
import java.util.UUID

object OfflineUtils {

    fun getRequestObject(): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        map[OfflineConstant.REQUEST_ID] = UUID.randomUUID().toString()
        map[OfflineConstant.APP_VERSION_NAME] = BuildConfig.VERSION_NAME
        map[OfflineConstant.APP_VERSION_CODE] = BuildConfig.VERSION_CODE
        return map
    }
}