package com.medtroniclabs.spice.data.resource

import com.medtroniclabs.spice.common.SecuredPreference

data class RequestAllEntities(
    var villageIds: List<Long> = listOf(),
    val lastSyncedTime: String? = null,
    val lastSyncedUserId: String = SecuredPreference.getUserFhirId()
)