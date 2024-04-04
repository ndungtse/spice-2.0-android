package com.medtroniclabs.spice.data.model

data class AboveFiveYearsSubmitRequest(
    val assessmentType: String? = null,
    val patientId: String,
    val latitude: Double,
    val longitude: Double,
    val householdId: Long,
    val memberId: String? = null,
    val referred: Boolean = true,
    val provenance :ProvenanceRequestItem? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val presentingComplaints: List<String?>? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: List<String?>? = null,
    val systemicExaminationsNotes: String? = null,
    val clinicalNotes: String? = null
    )

data class ProvenanceRequestItem(
    val createdDataTime: String?= null,
    val userId: Long?= null,
    val organizationId: Long?= null
)
