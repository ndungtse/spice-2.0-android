package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.model.MedicalReviewEncounter

data class WhoClinicalStageCreateRequest(
    val encounter: MedicalReviewEncounter,
    val stringValue: String,
)

data class HivClinicalInfoResponse(
    val stringValue: String? = null,
    val cd4: String? = null,
    val cd4Percentage: String? = null,
    val weight: Double? = null,
    val height: Double? = null,
)
