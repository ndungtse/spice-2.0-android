package org.medtroniclabs.uhis.data.model

data class PatientTypeCreateRequest(
    val encounter: MedicalReviewEncounter? = null,
    val patientReference: String? = null,
    val stringValue: String? = null,
)
