package com.medtroniclabs.spice.network

import com.medtroniclabs.spice.BuildConfig

object NetworkConstants {
    const val BASE_URL = BuildConfig.API_BASE_URL
    const val AUTH_SESSION = "/auth-service"
    const val OFFLINE_SYNC_CREATE = "/offline-service/offline-sync/create"
}