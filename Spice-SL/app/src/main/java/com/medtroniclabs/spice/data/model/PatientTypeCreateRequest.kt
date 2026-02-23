package com.medtroniclabs.spice.data.model

data class PatientTypeCreateRequest(
    val encounter: MedicalReviewEncounter? = null,
    val patientReference: String? = null,
    val stringValue: String? = null,
)
