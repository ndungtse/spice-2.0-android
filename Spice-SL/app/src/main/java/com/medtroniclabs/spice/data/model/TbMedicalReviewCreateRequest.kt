package com.medtroniclabs.spice.data.model

data class TbMedicalReviewCreateRequest(
    val encounter: MedicalReviewEncounter? = null,
    val presentingComplaints: List<String?>? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: List<ChipResponse>? = null,
    val systemicExaminationsNotes: String? = null,
    val comorbidities: List<String?>? = null,
    val comorbiditiesNotes: String? = null,
    val clinicalNotes: String? = null,
    val presumptiveTbNo: String? = null,
    var id: String? = null,
)

data class ChipResponse(
    val name: String? = null,
    val value: String? = null,
)
