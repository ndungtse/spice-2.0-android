package com.medtroniclabs.spice.data.offlinesync.model

import com.medtroniclabs.spice.db.entity.FollowUp

data class ResponseInitialDownload(
    val households: List<HouseHold>,
    val members: List<HouseHoldMember>,
    val pregnancyInfos: List<PregnancyDetails>,
    val followUps: List<FollowUp>,
    val followUpCriteria: FollowUpCriteria,
    val followUpCallAttempts: Int,
    val referredFollowUpDays: Int
)
