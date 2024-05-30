package com.medtroniclabs.spice.data

data class AboveFiveYearsSummaryDetails(
    val id: String,
    val encounterId: String? = null,
    val presentingComplaints: ArrayList<String>? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: ArrayList<String>? = null,
    val systemicExaminationsNotes: String? = null,
    val clinicalNotes: String? = null,
    val patientReference: String? = null
)
