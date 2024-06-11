package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class PatientStatusRequest(
    val patientId: String? = null,
    val type: String? = null,
    val gender: String? = null,
    val isPregnant: Boolean,
    val provenance: ProvanceDto
)
