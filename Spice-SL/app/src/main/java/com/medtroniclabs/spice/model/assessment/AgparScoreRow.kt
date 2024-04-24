package com.medtroniclabs.spice.model.assessment

import com.medtroniclabs.spice.ui.mypatients.enumType.AgparRowIdentifierType

data class AgparScoreRow(
    val indicatorType: AgparRowIdentifierType,
    val indicatorName: Int,
    val oneMinute: String? = null,
    val fiveMinute: String? = null,
    val tenMinute: String? = null
)
