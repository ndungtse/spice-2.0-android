package org.medtroniclabs.uhis.data

import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto

data class PatientStatusRequest(
    val patientId: String? = null,
    val memberId: String,
    val type: String? = null,
    val gender: String? = null,
    val ticketType: String,
    val isPregnant: Boolean,
    val encounterType: String? = null,
    val provenance: ProvanceDto,
)
