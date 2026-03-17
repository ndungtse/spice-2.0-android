package org.medtroniclabs.uhis.model.medicalreview

data class ResponseCreateImmunisation(
    val patientReference: String,
    val encounterId: String,
)
