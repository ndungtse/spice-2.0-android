package com.medtroniclabs.spice.data

data class MotherNeonateAncSummaryModel(
    val id: String? = null,
    val presentingComplaints: List<String>? = null,
    val presentingComplaintsNotes: String? = null,
    val obstetricExaminations: List<String>? = null,
    val obstetricExaminationNotes: String? = null,
    val clinicalNotes: String? = null,
    val bmi: Double? = null,
    val fundalHeight: Double? = null,
    val fetalHeartRate: Double? = null
)