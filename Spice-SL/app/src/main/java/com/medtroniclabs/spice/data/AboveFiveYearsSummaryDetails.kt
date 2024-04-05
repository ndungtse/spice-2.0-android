package com.medtroniclabs.spice.data

data class AboveFiveYearsSummaryDetails(
    val id: String,
    val presentingComplaints: ArrayList<String>? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: ArrayList<String>? = null,
    val systemicExaminationsNotes: String? = null,
    val clinicalNotes: String? = null
)
