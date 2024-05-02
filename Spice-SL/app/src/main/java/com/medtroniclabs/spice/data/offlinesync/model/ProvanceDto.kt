package com.medtroniclabs.spice.data.offlinesync.model

import com.google.gson.annotations.SerializedName
import com.medtroniclabs.spice.common.SecuredPreference


data class ProvanceDto(
    val userId: String = SecuredPreference.getUserFhirId(),
    val organizationId: String = SecuredPreference.getOrganizationFhirId(),
    @SerializedName("createdDataTime") //TODO remove this when BE renamed properly
    var createdDateTime: String = ""
)
