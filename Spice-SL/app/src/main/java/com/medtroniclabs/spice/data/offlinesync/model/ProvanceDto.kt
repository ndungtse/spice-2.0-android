package com.medtroniclabs.spice.data.offlinesync.model

import com.medtroniclabs.spice.appextensions.convertToUtcDateTime
import com.medtroniclabs.spice.common.SecuredPreference


data class ProvanceDto(
    val userId: String = SecuredPreference.getUserFhirId(),
    val organizationId: String = SecuredPreference.getOrganizationFhirId(),
    var modifiedDate: String = System.currentTimeMillis().convertToUtcDateTime(),
    val spiceUserId: Long = SecuredPreference.getUserId()
)
