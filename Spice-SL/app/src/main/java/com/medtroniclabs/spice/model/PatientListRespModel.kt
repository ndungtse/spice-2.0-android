package com.medtroniclabs.spice.model

data class PatientListRespModel(
    val id: Long? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val patientId: Long? = null,
    val village: String? = null,
    val nationalID: Long? = null
)