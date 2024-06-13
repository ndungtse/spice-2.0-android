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
    val landmark:String? = null,
    val fhirUrl: String? = null,
    val performer: String? = null,
    val chwName: String? = null,
    val houseHoldId: String? = null,
    val houseHoldNumber: Long? = null,
    val dateOfOnset:String? = null,
    val pregnancyDetails: PregnancyDetails? = null,
    val villageId:String? = null,
)

data class PregnancyDetails(
    val lastMenstrualPeriod: String? = null,
    val ancVisitAssessment: Long? = null,
    val pncVisitAssessment: Long? = null,
    val childHoodVisitAssessment: Long? = null,
    val ancVisitMedicalReview: Long? = null,
    val pncVisitMedicalReview: Long? = null,
    val childHoodVisitMedicalReview: Long? = null,
    val patientStatus: String? = null,
    val height: Double? = null,
    val pulse: Double? = null,
    val estimatedDeliveryDate: String? = null,
    val gestationalAge: String? = null,
    val noOfFetus: Int? = null,
    val gravida: Int? = null,
    val parity: Int? = null,
    val patientBloodGroup: String? = null,
    val bmi: Double? = null,
    val pregnant: Boolean? = null,
    val ancVisit:String? = null,
    val villageId: String? = null
)
data class SearchAndListResponse(
    val patientList: List<PatientListRespModel> = emptyList(),
    val referencePatientId: String? = null
)