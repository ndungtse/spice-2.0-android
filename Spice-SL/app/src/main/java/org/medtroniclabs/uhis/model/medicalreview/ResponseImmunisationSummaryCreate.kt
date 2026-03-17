package org.medtroniclabs.uhis.model.medicalreview

data class ResponseImmunisationSummaryCreate(
    val patientId: String,
    val encounterId: Long,
)
