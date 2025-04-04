package com.medtroniclabs.spice.data.offlinesync.model

data class ResponseRxBuddyRegister(
    val relationShip: String,
    val otherRelationship: String? = null,
    val isMonitorSheetProvided: Boolean,
    var householdMemberId: String? = null,
    var rxBuddyDetails: RxBuddyMember? = null,
    val nextVisitDate: String = ""
)
