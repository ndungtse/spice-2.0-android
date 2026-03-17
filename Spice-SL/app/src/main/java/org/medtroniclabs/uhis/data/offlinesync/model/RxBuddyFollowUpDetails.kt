package org.medtroniclabs.uhis.data.offlinesync.model

import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.offlinesync.utils.OfflineSyncStatus

data class RxBuddyFollowUpDetails(
    var id: Long = 0,
    val rxBuddyLocalId: Long,
    val rxBuddyId: Long?,
    var patientMemberId: String,
    var followUp: String? = "",
    var nextVisitDate: String = "",
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
