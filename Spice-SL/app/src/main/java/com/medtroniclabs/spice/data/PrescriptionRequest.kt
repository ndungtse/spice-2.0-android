package com.medtroniclabs.spice.data

data class PrescriptionRequest(
    val encounter: EncounterDetails,
    val prescriptions: List<Prescription>
)

data class EncounterDetails(
    val id: String,
    val patientReference: String,
    val patientId: String,
    val memberId: String
)

data class Prescription(
    val type: String = "",
    val prescribedDays: Long,
    val medicationName: String,
    val medicationId: String,
    val dosage: String = "",
    val unit: String = "",
    val form: String,
    val frequency: Int,
    val instructionToTake: String,
    val prescribedSince: String
)