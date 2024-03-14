package com.medtroniclabs.spice.offlinesync.utils

import java.util.UUID

object OfflineUtils {

    fun getRequestObject(): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        map[OfflineConstant.REQUEST_ID] = UUID.randomUUID().toString()
        return map
    }
}