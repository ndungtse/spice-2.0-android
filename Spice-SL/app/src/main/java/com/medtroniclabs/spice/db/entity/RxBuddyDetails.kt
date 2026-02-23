package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus

@Entity(tableName = EntitiesName.RX_BUDDY_DETAILS)
data class RxBuddyDetails(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var rxBuddyId: Long? = null,
    var patientMemberId: String,
    var householdMemberId: Long? = null,
    var name: String? = null,
    var phoneNumber: String? = null,
    var relationship: String,
    var isMonitorSheetProvider: Boolean,
    val nextVisitDate: String,
    var otherRelationship: String? = null,
    val followUpId: Long? = null,
    var isActive: Boolean = true,
    var syncStatus: OfflineSyncStatus = OfflineSyncStatus.NotSynced,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val createdBy: Long = SecuredPreference.getUserId(),
    var updatedAt: Long = System.currentTimeMillis(),
)
