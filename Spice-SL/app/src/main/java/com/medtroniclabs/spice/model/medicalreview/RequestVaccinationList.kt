package com.medtroniclabs.spice.model.medicalreview

data class RequestVaccinationList(
    val patientReference: String?,
    val memberId: String?,
    val patientId: String?,
    val birthDate: String?,
)
