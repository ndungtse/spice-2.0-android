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
    val occupation:String? =null,
    val location:String? = null,
    val fhirUrl: String? = null,
    val performer: String? = null,
    val chw: String? = null,
    val houseHoldId: Long? = null,
    val dateOfOnset:String? = null,
    val lastMenstrualPeriod:String? = null,
    val ancVisit:String? = null
)

data class SearchAndListResponse(
    val patientList: List<PatientListRespModel> = emptyList(),
    val referencePatientId: String? = null
)