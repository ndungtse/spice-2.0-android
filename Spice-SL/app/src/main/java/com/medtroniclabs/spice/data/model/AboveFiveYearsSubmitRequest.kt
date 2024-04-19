package com.medtroniclabs.spice.data.model

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class AboveFiveYearsSubmitRequest(
    val assessmentType: String? = null,
    val patientId: String,
    val latitude: Double,
    val longitude: Double,
    val householdId: Long,
    val memberId: String,
    val referred: Boolean = true,
    val provenance: ProvanceDto? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val presentingComplaints: List<String?>? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: List<String?>? = null,
    val systemicExaminationsNotes: String? = null,
    val clinicalNotes: String? = null
)

