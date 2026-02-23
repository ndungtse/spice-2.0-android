package com.medtroniclabs.spice.model.medicalreview

data class ResponseImmunisationSummaryCreate(
    val patientId: String,
    val encounterId: Long,
)
