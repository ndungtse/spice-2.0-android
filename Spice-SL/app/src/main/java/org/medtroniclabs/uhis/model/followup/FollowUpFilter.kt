package org.medtroniclabs.uhis.model.followup

import org.medtroniclabs.uhis.data.model.ChipViewItemModel

data class FollowUpFilter(
    var search: String = "",
    var type: String = "", // HH_VISIT, REFERRED, MEDICAL_REVIEW
    var villages: List<Long> = listOf(),
    var selectedVillages: List<ChipViewItemModel>? = null,
    var selectedDateRange: List<ChipViewItemModel>? = null,
    var selectedReasons: List<ChipViewItemModel>? = null,
    var fromDate: String = "",
    var toDate: String = "",
)
