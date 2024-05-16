package com.medtroniclabs.spice.data.offlinesync.model

import com.medtroniclabs.spice.db.entity.FollowUp

data class ResponseInitialDownload(
    val households: List<HouseHold>,
    val members: List<HouseHoldMember>,
    val followUpList: List<FollowUp>,
    val pregnancyInfos: List<PregnancyDetails>,
    val followUpCallAttempts: Int,
    val referredFollowUpDays: Int
)
