package com.medtroniclabs.spice.db.converters

import androidx.room.TypeConverter
import com.medtroniclabs.spice.offlinesync.utils.OfflineSyncStatus

class OfflineStatusTypeConverter {

    @TypeConverter
    fun fromOfflineSyncStatus(syncStatus: OfflineSyncStatus): String {
        return syncStatus.name
    }

    @TypeConverter
    fun toOfflineSyncStatus(syncStatusString: String): OfflineSyncStatus {
        return OfflineSyncStatus.valueOf(syncStatusString)
    }
}