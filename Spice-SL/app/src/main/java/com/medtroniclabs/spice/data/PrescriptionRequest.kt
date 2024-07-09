package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class PrescriptionRequest(
    val encounter: EncounterDetails,
    val prescriptions: List<Prescription>
)

data class EncounterDetails(
    val id: String? = null,
    val patientReference: String?,
    val patientId: String,
    val memberId: String,
    val provenance: ProvanceDto,
)

data class Prescription(
    val prescribedDays: Long,
    val medicationName: String,
    val medicationId: String,
    val frequency: Int,
    val prescribedSince: String,
    val prescriptionId: String? = null,
    val endDate: String? = null,
    val discontinuedDate: String? = null,
    var codeDetails: CodeDetailsObject? = null
)