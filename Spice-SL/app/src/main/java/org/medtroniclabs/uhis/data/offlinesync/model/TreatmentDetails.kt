package org.medtroniclabs.uhis.data.offlinesync.model

data class TreatmentDetails(
    val memberId: String,
    val treatmentStartDate: String?,
    val dateDiagnosed: String?,
    val diagnosis: String?,
    val prescriptions: List<Prescription>?,
    val isTbConfirmed: Boolean?,
    val tbConfirmationDate: String?,
)
