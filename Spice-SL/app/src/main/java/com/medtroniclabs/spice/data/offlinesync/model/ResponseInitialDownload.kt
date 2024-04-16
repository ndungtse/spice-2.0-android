package com.medtroniclabs.spice.data.offlinesync.model

data class ResponseInitialDownload(
    val households: List<HouseHold>,
    val members: List<HouseHoldMember>
)
