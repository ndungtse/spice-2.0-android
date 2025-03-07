package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class DiagnosisSaveUpdateRequest(
    val patientId: String,
    val patientReference: String?,
    val diseases: ArrayList<DiagnosisDiseaseModel>,
    val provenance: ProvanceDto,
    val otherNotes: String? = null,
    val type: String
)

data class DiagnosisDiseaseModel(
    val diseaseCategoryId: Long,
    val diseaseConditionId: Long?,
    val diseaseCategory: String,
    val notes: String? = null,
    val diseaseCondition: String?,
    val siteOfDisease:Boolean = false
)
