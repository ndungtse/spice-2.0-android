package com.medtroniclabs.spice.data.history

import com.medtroniclabs.spice.model.ReferredDate

data class MedicalReviewHistory(
    val id: String? = null,
    val patientReference: String? = null,
    val dateOfReview: String? = null,
    val reviewDetails: ReviewDetails? = null,
    val history: List<ReferredDate>? = null,
    val type: String? = null
)

data class ReviewDetails(
    val id: String? = null,
    val visitNumber: Int? = null,
    val patientReference: String? = null,
    val diagnosis: List<DiseaseInfo>? = null,
    val patientStatus: String? = null,
    val isMotherAlive: Boolean? = null,
    val breastCondition: String? = null,
    val breastConditionNotes: String? = null,
    val involutionsOfTheUterus: String? = null,
    val involutionsOfTheUterusNotes: String? = null,
    val presentingComplaints: Any? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: List<String?>? = null,
    val systemicExamination: List<String?>? = null,
    val obstetricExaminations: List<String?>? = null,
    val systemicExaminationsNotes: String? = null,
    val systemicExaminationNotes: String? = null,
    val obstetricExaminationNotes: String? = null,
    val clinicalNotes: String? = null,
    val labourDTO:LabourDTO? = null,
    val neonateOutcome:String?=null,
    val stateOfBaby:String? = null,
    val birthWeight:String? = null,
    val signs:List<String?>? = null,
)

data class DiseaseInfo(
    val diseaseCategoryId: Long? = null,
    val diseaseConditionId: Long? = null,
    val diseaseCategory: String? = null,
    val notes: String? = null,
    val diseaseCondition: String? = null,
    val type: String? = null
)

data class LabourDTO(
    val dateAndTimeOfDelivery: String? = null,
    val dateAndTimeOfLabourOnset: String? = null,
    val deliveryType: String? = null,
    val deliveryBy: String? = null,
    val deliveryAt: String? = null,
    val deliveryStatus: String? = null,
)


