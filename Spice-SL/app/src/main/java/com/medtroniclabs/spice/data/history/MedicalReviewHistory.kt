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
    val presentingComplaints: List<String?>? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: List<String?>? = null,
    val obstetricExaminations: List<String?>? = null,
    val systemicExaminationsNotes: String? = null,
    val obstetricExaminationsNotes: String? = null,
    val clinicalNotes: String? = null,
)

data class DiseaseInfo(
    val diseaseCategoryId: Long? = null,
    val diseaseConditionId: Long? = null,
    val diseaseCategory: String? = null,
    val notes: String? = null,
    val diseaseCondition: String? = null,
    val type: String? = null
)


