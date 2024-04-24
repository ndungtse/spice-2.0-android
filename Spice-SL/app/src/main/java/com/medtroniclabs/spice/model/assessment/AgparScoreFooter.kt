package com.medtroniclabs.spice.model.assessment

data class AgparScoreFooter(
    val indicatorName: Int,
    val oneMinuteTotal: String? = null,
    val fiveMinuteTotal: String? = null,
    val tenMinuteTotal: String? = null
)
