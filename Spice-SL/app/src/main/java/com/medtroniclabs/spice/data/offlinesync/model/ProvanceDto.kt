package com.medtroniclabs.spice.data.offlinesync.model

import com.medtroniclabs.spice.common.SecuredPreference


data class ProvanceDto(
    val userId: String = SecuredPreference.getUserFhirId(),
    val organizationId: String = SecuredPreference.getOrganizationFhirId(),
    var createdDateTime: String = ""
)
