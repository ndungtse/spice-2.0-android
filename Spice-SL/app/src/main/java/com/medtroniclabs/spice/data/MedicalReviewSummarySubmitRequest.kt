package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class MedicalReviewSummarySubmitRequest(
    val patientId: String? = null,
    val patientReference: String? = null,
    val memberId: String? = null,
    val id: String? = null,
    val provenance: ProvanceDto,
    val cost: String? = null,
    val medicalSupplies: List<String>? = null,
    val patientStatus: String? = null,
    val nextVisitDate: String? = null,
    val category: String? = null,
    val encounterType: String? = null,
    val householdId: String? = null,
    val villageId: String? = null,
    val tbIMRCompleted: Boolean? = null,
    val treatmentOutcome: String? = null,
    val maternalOutcome: String? = null,
    val emtctVisitStatus: String? = null,
)
