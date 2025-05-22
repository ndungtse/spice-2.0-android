package com.medtroniclabs.spice.model

data class ViralLoadRecord( val label: String?,
                            val collectionDate: String?,
                            val gestationAtDate: String?,
                            val result: String?
)
data class ArtRequest(
    val limit: Int,
    val category: String,
    val isActive: Boolean,
    val patientReference: String?
)
data class ARTResponse(
    val prescribedDays: Int,
    val medicationName: String,
    val medicationId: String,
    val frequency: Int,
    val frequencyName: String,
    val prescribedSince: String,
    val endDate: String,
    val prescriptionId: String,
    val discontinuedReason: String?,
    val discontinuedDate: String?,
    val encounterId: String,
    val isActive: Boolean,
    val isDeleted: Boolean,
    val codeDetails: String?,
    val categoryName: String?,
    val groupName: String?,
    val groupUniqueId: String?,
    val instruction: String?,
    val classificationName: String?,
    val brandName: String?,
    val dosageFrequencyName: String?,
    val dosageUnitName: String?,
    val dosageUnitValue: String?,
    val instructionNote: String?,
    val dosageFormName: String?,
    val prescriptionRemainingDays: Int,
    val prescriptionFilledDays: Int,
    val dispenseRemainingDays: Int,
    val lastReFillDate: String?,
    val reason: String?,
    val regimenLine:String?,
    val reasonsForChange:String?
)
data class ARTLoadRecord( val startDate: String?,
                            val endDate: String?,
                            val regimen: String?,
                            val regimenLine: String?,
                            val reasonForChange: String?
)
