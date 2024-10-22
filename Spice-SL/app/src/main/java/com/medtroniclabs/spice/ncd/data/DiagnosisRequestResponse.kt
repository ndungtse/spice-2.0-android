package com.medtroniclabs.spice.ncd.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class NCDDiagnosisRequestResponse(
    val provenanceDTO: ProvanceDto? = null,
    val confirmDiagnosis: List<NCDDiagnosisItem>? = null,
    val diagnosis: List<NCDDiagnosisItem>? = null,
    val diagnosisNotes: String? = null,
    val patientReference: String? = null
)

data class NCDDiagnosisItem(
    val type: String? = null,
    val value: String? = null,
    val name: String? = null
)

data class NCDDiagnosisGetRequest(
    val patientReference: String? = null,
    val diagnosisType: List<String>? = null
)

data class NCDDiagnosisGetResponse(
    val diagnosis: List<NCDDiagnosisItem>? = null,
    val diagnosisNotes: String? = null,
)
