package com.medtroniclabs.spice.model.medicalreview

data class CreateUnderTwoMonthsResponse(
    val encounterId: String? = null,
    val patientReference: String?
)

data class UnderTwoMonthsSummaryDetails(
    val id: String,
    val clinicalNotes: String?,
    val presentingComplaintsNotes: String?,
    val examination: Map<String, List<ExaminationDetail>>?
)

data class ExaminationDetail(
    val title: String?,
    val value: String?
)


