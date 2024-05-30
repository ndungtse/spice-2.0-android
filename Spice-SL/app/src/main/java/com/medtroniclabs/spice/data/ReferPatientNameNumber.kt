package com.medtroniclabs.spice.data

data class ReferPatientNameNumber(
    val id: String,
    val firstName: String,
    val roles: List<ReferPatientUserRoles>,
    val lastName: String,
    val phoneNumber: String,
    val fhirId: String,
)
