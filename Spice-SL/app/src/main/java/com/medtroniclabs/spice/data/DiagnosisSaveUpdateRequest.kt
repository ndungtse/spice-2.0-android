package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class DiagnosisSaveUpdateRequest(
    val patientId: String,
    val patientReferance: String,
    val diseases: ArrayList<DiagnosisDiseaseModel>,
    val provenance: ProvanceDto
)

data class DiagnosisDiseaseModel(
    val diseaseCategoryId: Long,
    val diseaseConditionId: Long,
    val diseaseCategory: String,
    val notes: String? = null,
    val diseaseCondition: String
)
