package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class SummarySubmitRequest(
    val id: String? = null,
    val memberId: String? = null,
    val patientReference: String? = null,
    val assessmentName: String? = null,
    val provenance: ProvanceDto? = null,
    val submitCreateId: String? = null,
    val nextVisitDate: String? = null,
    val patientStatus: String? = null,
    val householdId: String? = null,
    val villageId: String? = null,
    val encounter: MedicalReviewEncounter? = null,
    val referralTicketType: String? = null,
    val patientId: String? = null
)
