package com.medtroniclabs.spice.ncd.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class BadgeNotificationModel(
    val patientReference: String? = null,
    val menuName: String? = null,
    val nutritionLifestyleReviewedCount: Int = 0,
    val psychologicalCount: Int = 0,
    val nonReviewedTestCount: Int = 0,
    val prescriptionDaysCompletedCount: Int = 0,
    val provenance: ProvanceDto = ProvanceDto(),
)
