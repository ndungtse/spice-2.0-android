package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class SummarySubmitRequest(
    val id: String?=null,
    val memberId: String?=null,
    val patientReference: String?=null,
    val provenance: ProvanceDto?=null,
    val submitCreateId: String? = null,
    val nextVisitDate: String? = null,
    val patientStatus: String? = null
)
