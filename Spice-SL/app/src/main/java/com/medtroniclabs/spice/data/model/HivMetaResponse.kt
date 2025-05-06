package com.medtroniclabs.spice.data.model

import com.medtroniclabs.spice.data.MedicalReviewMetaItems

data class HivMetaResponse(
    val hivHistory:List<MedicalReviewMetaItems>,
    val populationType:List<MedicalReviewMetaItems>,
    val hivTestDurations:List<MedicalReviewMetaItems>,
    val entryPoint:List<MedicalReviewMetaItems>
)