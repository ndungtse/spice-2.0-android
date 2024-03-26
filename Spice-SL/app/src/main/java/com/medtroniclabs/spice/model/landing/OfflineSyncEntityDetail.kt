package com.medtroniclabs.spice.model.landing

data class OfflineSyncEntityDetail(
    val tableName: String,
    var unSyncedCount: Int
)
