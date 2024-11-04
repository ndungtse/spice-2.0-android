package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class EncounterDetails(
    val id: String? = null,
    val patientReference: String? = null,
    val patientId: String? = null,
    val memberId: String? = null,
    val provenance: ProvanceDto,
    val patientVisitId : String? = null
)