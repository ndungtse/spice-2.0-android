package com.medtroniclabs.spice.data.model

data class TbMedicalReviewCreateRequest(
    val encounter: MedicalReviewEncounter? = null,
    val presentingComplaints: List<String?>? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: List<String?>? = null,
    val systemicExaminationsNotes: String? = null,
    val comorbidities: List<String?>? = null,
    val clinicalNotes: String? = null
)
