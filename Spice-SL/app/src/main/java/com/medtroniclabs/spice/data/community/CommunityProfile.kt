package com.medtroniclabs.spice.data.community

data class CommunityProfile(
    val villageId:Long,
    val villageName:String?,
    val houseHoldCount:Int?=0,
)
