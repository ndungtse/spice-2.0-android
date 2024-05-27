package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class AboveFiveYearsSummarySubmitRequest(
    val assessmentType: ArrayList<String>? = null,
    val patientId: String? = null,
    val patientReference: String? = null,
    val memberId: String? = null,
    val id: String? = null,
    val provenance: ProvanceDto,
    val cost: String? = null,
    val medicalSupplies: List<String>? = null,
    val patientStatus: String? = null,
    val nextVisitDate: String? = null,
    val referralTicketType:String? = null
)
