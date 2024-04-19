package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class AboveFiveYearsSummarySubmitRequest(
    val assessmentType: ArrayList<String>? = null,
    val patientId: String,
    val patientReference: String?=null,
    val memberId: String,
    val id: String,
    val provenance: ProvanceDto,
    val cost: String? = null,
    val medicalSupplies: List<String>? = null,
    val patientStatus: String? = null,
    val nextVisitDate:String? = null
)
