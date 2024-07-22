package com.medtroniclabs.spice.data


data class PrescriptionRequest(
    val encounter: EncounterDetails,
    val prescriptions: List<Prescription>
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
    var codeDetails: CodeDetailsObject? = null,
    var frequencyName: String
)