package com.medtroniclabs.spice.data.offlinesync.model

data class RxBuddyFollowUp(
    val id: Long = 0,
    val rxBuddyId: Long? = 0,
    val monitoringSheet: List<String>,
    val isSymptomsGettingWorse: Boolean,
    val hadReactionToYourMedications: Boolean,
    val nextVisitDate: String = "",
    val followUpId: Long? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val provenance: ProvanceDto = ProvanceDto()
)
