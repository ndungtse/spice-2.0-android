package com.medtroniclabs.spice.model

data class PatientListRespModel(
    val id: String? = null,
    val name: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val birthDate: String? = null,
    val patientId: String? = null,
    val village: String? = null,
    val nationalID: Long? = null,
    val phoneNumber: String? = null,
    val memberId: String? = null,
    val fhirUrl: String? = null,
    val performer: String? = null,
    val houseHoldId: String? = null,
    val chw:String? = null
)