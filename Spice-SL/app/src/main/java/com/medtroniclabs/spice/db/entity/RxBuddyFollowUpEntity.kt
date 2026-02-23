package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus

@Entity(tableName = EntitiesName.RX_BUDDY_FOLLOW_UP_ENTITY)
data class RxBuddyFollowUpEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val rxBuddyLocalId: Long,
    val rxBuddyId: Long? = null,
    var patientMemberId: String,
    var followUp: String? = "",
    var nextVisitDate: String = "",
    val followUpId: Long? = null,
    var syncStatus: OfflineSyncStatus = OfflineSyncStatus.NotSynced,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val createdBy: Long = SecuredPreference.getUserId(),
    var updatedAt: Long = System.currentTimeMillis(),
)
