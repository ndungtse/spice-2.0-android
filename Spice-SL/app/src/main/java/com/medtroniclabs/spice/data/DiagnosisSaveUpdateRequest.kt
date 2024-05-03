package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class DiagnosisSaveUpdateRequest(
    val patientId: String,
    val patientReference: String,
    val disease: ArrayList<DiagnosisDiseaseModel>,
    val provenance: ProvanceDto
)

data class DiagnosisDiseaseModel(
    val diseaseCategoryId: Long,
    val diseaseConditionId: Long,
    val diseaseCategory: String,
    val notes: String,
    val diseaseCondition: String
)
