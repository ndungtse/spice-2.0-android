package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class EncounterDetails(
    val id: String? = null,
    val visitId: String? = null,
    val patientReference: String?,
    val patientId: String,
    val memberId: String,
    val provenance: ProvanceDto,
)