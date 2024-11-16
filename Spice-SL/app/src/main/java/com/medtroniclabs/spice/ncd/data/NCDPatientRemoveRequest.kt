package com.medtroniclabs.spice.ncd.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class NCDPatientRemoveRequest(
    val patientId: String? = null,
    val reason: String? = null,
    val provenance: ProvanceDto = ProvanceDto(),
    val otherReason: String? = null
)