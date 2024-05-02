package com.medtroniclabs.spice.data.resource

import com.medtroniclabs.spice.common.SecuredPreference

data class RequestAllEntities(
    var villageIds: List<Long> = listOf(),
    val lastSyncedTime: String? = null,
    val lastSyncedUser: LastSyncedUser = LastSyncedUser()
)

data class LastSyncedUser(
    val fhirId: String = SecuredPreference.getUserFhirId(),
    val id: Int = 0
)