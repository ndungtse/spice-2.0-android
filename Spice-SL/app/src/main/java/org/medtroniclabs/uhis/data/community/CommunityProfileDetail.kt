package org.medtroniclabs.uhis.data.community

data class CommunityProfileDetail(
    val villageId: Long,
    val villageName: String?,
    val houseHoldCount: Int? = 0,
    val isCommunityProfileDetailAvailable: Int? = 0,
)
