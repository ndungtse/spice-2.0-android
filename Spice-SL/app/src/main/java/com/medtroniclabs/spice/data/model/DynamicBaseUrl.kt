package com.medtroniclabs.spice.data.model

// Data class for the request body
data class BaseUrlRequest(
    val versionCode: String,
    val appVersion: String,
    val deviceId: String?,
)

//    data class BaseUrlResponse(val url: String)
data class BaseUrlResponse(
    val url: String? = null,
    val versionCode: String? = null,
)
