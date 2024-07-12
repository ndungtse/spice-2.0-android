package com.medtroniclabs.spice.network

object NetworkConstants {
    const val BASE_URL = BaseUrl.DEV
    const val AUTH_SESSION = "/auth-service"
    const val OFFLINE_SYNC_CREATE = "/offline-service/offline-sync/create"
}

object BaseUrl {
    const val LOCAL = "http://192.168.23.129/"
    const val DEV = "https://spice-dev-backend.sl.labsplatform.com/"
    const val TRAINING = "https://spice-training-backend.sl.labsplatform.com/"
}