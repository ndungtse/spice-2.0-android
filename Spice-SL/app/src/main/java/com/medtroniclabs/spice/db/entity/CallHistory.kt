package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.EntitiesName.CALL_HISTORY

@Entity(tableName = CALL_HISTORY)
data class CallHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val referenceId: String,
    val callStartTime: String,
    val callEndTime: String,
    val syncStatus: OfflineSyncStatus = OfflineSyncStatus.NotSynced
)
