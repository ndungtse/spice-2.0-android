package com.medtroniclabs.spice.ncd.data

data class NCDSupportRequest(
    val userId: String,
    val summary: String,
    val healthFacilityId:Long
)
