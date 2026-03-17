package org.medtroniclabs.uhis.model.assessment

import org.medtroniclabs.uhis.ui.mypatients.enumType.AgparItemViewType

data class ApgarScore(
    val viewType: AgparItemViewType,
    val header: AgparScoreHeader? = null,
    val row: AgparScoreRow? = null,
    val footer: AgparScoreFooter? = null,
)
