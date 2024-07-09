package com.medtroniclabs.spice.model.medicalreview

import com.medtroniclabs.spice.data.DiagnosisDiseaseModel

data class CreateUnderTwoMonthsResponse(
    val encounterId: String? = null,
    val patientReference: String?,
    val type: String? = null
)


data class SummaryDetails(
    val id: String,
    val clinicalNotes: String?,
    val presentingComplaintsNotes: String?,
    val examination: Map<String, List<ExaminationDetail>>?,
    val patientStatus:String?,
    val diagnosis: ArrayList<DiagnosisDiseaseModel>? = null
)

data class ExaminationDetail(
    val title: String?,
    val value: String?
)


