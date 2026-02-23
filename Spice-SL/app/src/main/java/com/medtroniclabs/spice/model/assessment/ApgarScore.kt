package com.medtroniclabs.spice.model.assessment

import com.medtroniclabs.spice.ui.mypatients.enumType.AgparItemViewType

data class ApgarScore(
    val viewType: AgparItemViewType,
    val header: AgparScoreHeader? = null,
    val row: AgparScoreRow? = null,
    val footer: AgparScoreFooter? = null,
)
