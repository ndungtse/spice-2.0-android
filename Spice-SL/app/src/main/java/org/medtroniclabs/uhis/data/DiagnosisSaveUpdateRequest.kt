package org.medtroniclabs.uhis.data

import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto

data class DiagnosisSaveUpdateRequest(
    val patientId: String,
    val patientReference: String?,
    val diseases: ArrayList<DiagnosisDiseaseModel>,
    val provenance: ProvanceDto,
    val otherNotes: String? = null,
    val type: String,
)

data class DiagnosisDiseaseModel(
    val diseaseCategoryId: Long,
    val diseaseConditionId: Long?,
    val diseaseCategory: String,
    val notes: String? = null,
    val diseaseCondition: String?,
    var type: String? = null,
)
