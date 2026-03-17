package org.medtroniclabs.uhis.data.offlinesync.model

import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.offlinesync.utils.OfflineSyncStatus

data class RxBuddyRegisterDetail(
    var id: Long = 0,
    var rxBuddyId: Long? = null,
    var patientMemberId: String,
    var householdMemberId: Long? = null,
    var name: String? = null,
    var phoneNumber: String? = null,
    var relationship: String,
    var otherRelationship: String? = null,
    var isMonitorSheetProvider: Boolean,
    var nextVisitDate: String,
    var isActive: Boolean = true,
    var followUpId: Long? = null,
    var syncStatus: OfflineSyncStatus = OfflineSyncStatus.NotSynced,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val createdBy: Long = SecuredPreference.getUserId(),
    var updatedAt: Long = System.currentTimeMillis(),
    val patientId: String?,
    val villageId: String?,
    val householdId: String? = null,
)
