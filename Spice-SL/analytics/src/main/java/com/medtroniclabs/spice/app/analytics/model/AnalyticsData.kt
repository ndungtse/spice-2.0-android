package com.medtroniclabs.spice.app.analytics.model

import com.google.gson.JsonElement
import com.medtroniclabs.spice.app.analytics.utils.UserDetail

data class AnalyticsData(
    val deviceDetails: DeviceDetail = DeviceDetail(),
//    val userAnalytics: Map<String, MutableMap<String, MutableList<JsonElement>>>,
    val userAnalytics: List<AnalyticsDetail>

)

data class AnalyticsDetail(
    val id: String,
    val lastSyncDate: String,
    val analytics: MutableMap<String, MutableList<JsonElement>>
)

data class DeviceDetail(
    val appVersion: String = UserDetail.appVersion,
    val deviceName: String = android.os.Build.MODEL,
    val apiVersion: Int = android.os.Build.VERSION.SDK_INT
)