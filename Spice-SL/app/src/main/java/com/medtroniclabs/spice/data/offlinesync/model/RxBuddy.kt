package com.medtroniclabs.spice.data.offlinesync.model

data class RxBuddy(
    val patientMemberId: String,
    val patientId: String?,
    val villageId: String?,
    val householdId: String? = null,
    val id: Long? = null, // RxBuddy Backend Id
    val referenceId: Long, // RxBuddy Local Db Id
    val registry: RxBuddyRegister? = null,
    val followUps: List<RxBuddyFollowUp>,
    val provenance: ProvanceDto = ProvanceDto(),
)
