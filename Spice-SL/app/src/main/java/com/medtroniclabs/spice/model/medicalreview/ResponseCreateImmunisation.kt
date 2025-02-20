package com.medtroniclabs.spice.model.medicalreview

data class ResponseCreateImmunisation(
    val patientReference: String,
    val encounterId: String
)
