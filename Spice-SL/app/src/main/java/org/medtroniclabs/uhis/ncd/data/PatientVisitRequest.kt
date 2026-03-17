package org.medtroniclabs.uhis.ncd.data

import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto

data class PatientVisitRequest(
    val patientReference: String? = null,
    val provenance: ProvanceDto,
    val memberReference: String? = null,
)

data class PatientVisitResponse(
    val encounterReference: String? = null,
    val initialReviewed: Boolean? = false,
)
