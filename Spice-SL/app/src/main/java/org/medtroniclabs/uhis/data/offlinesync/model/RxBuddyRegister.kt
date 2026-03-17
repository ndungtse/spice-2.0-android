package org.medtroniclabs.uhis.data.offlinesync.model

data class RxBuddyRegister(
    val id: Long = 0,
    val referenceId: Long,
    val relationShip: String,
    val otherRelationship: String? = null,
    val isMonitorSheetProvided: Boolean,
    var householdMemberId: String? = null, // Synced Household member
    var rxBuddyDetails: RxBuddyMember? = null, // New Rx Buddy member
    val nextVisitDate: String = "",
    val followUpId: Long? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val provenance: ProvanceDto = ProvanceDto(),
)
