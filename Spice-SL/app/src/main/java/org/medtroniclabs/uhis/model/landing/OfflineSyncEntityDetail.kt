package org.medtroniclabs.uhis.model.landing

data class OfflineSyncEntityDetail(
    val tableName: String,
    var unSyncedCount: Int,
)
