package com.medtroniclabs.spice.data.model

data class RegistrationResponse(
    val dateOfEnrollment: String? = null,
    val name: String? = null,
    val gender: String? = null,
    val age: String? = null,
    val programId: String? = null,
    val nationalId: String? = null,
    val phoneNumber: String? = null,
    val facilityName: String? = null,
    val treatmentPlanResponse: Map<String, Any>? = null
)
