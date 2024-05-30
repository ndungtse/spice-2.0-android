package com.medtroniclabs.spice.data.model

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class AboveFiveYearsSubmitRequest(
    val assessmentType: String? = null,
    val presentingComplaints: List<String?>? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: List<String?>? = null,
    val systemicExaminationsNotes: String? = null,
    val clinicalNotes: String? = null,
    val encounter:MedicalReviewEncounter? = null,
)

