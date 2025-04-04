package com.medtroniclabs.spice.data.offlinesync.model

import androidx.compose.runtime.internal.StabilityInferred

data class RxBuddyRegister(
    val id: Long = 0,
    val referenceId: Long,
    val relationShip: String,
    val otherRelationship: String? = null,
    val isMonitorSheetProvided: Boolean,

    var householdMemberId: String? = null, // Synced Household member
    var householdMember: HouseHoldMember? = null, // Unsynced household member
    var rxBuddyDetails: RxBuddyMember? = null, // New Rx Buddy member
    val nextVisitDate: String = "",
    val followUpId: Long? = null,
    val provenance: ProvanceDto = ProvanceDto()
)
