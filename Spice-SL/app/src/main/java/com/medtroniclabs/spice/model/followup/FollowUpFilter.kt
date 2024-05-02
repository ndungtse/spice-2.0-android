package com.medtroniclabs.spice.model.followup

data class FollowUpFilter(
    var search: String = "",
    var type: String = "", //HH_VISIT, REFERRED, MEDICAL_REVIEW
    var villages: List<Long> = listOf(),
    var fromDate: String = "",
    var toDate: String = ""
)
