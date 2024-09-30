package com.medtroniclabs.spice.ncd.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class PatientVisitRequest(
    val patientReference: String? = null,
    val provenance: ProvanceDto,
    val memberReference: String? = null
)

data class PatientVisitResponse(
    val encounterReference: String? = null
)
