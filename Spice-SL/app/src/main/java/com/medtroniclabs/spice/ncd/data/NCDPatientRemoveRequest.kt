package com.medtroniclabs.spice.ncd.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class NCDPatientRemoveRequest(
    val patientId: String,
    val reason: String,
    val provenance: ProvanceDto,
    val otherReason: String? = null
)