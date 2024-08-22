package com.medtroniclabs.spice.network

object NetworkConstants {
    const val BASE_URL = BaseUrl.DEV
    const val AUTH_SESSION = "/auth-service"
    const val OFFLINE_SYNC_CREATE = "/offline-service/offline-sync/create"
}

object BaseUrl {
    const val LOCAL = "http://192.168.20.80/"
    const val DEV = "https://spice-dev-backend.sl.labsplatform.com/"
    const val DEV1 = "https://spice-dev-backend-v1.sl.labsplatform.com/"
    const val TRAINING = "https://spice-training-backend.sl.labsplatform.com/"
}