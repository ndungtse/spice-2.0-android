package com.medtroniclabs.spice.data.offlinesync.model

import com.google.gson.JsonObject
import com.medtroniclabs.spice.db.entity.FollowUp
import com.medtroniclabs.spice.db.entity.LinkHouseholdMember
import com.medtroniclabs.spice.db.entity.PregnancyDetail

data class ResponseInitialDownload(
    val households: List<HouseHold>?,
    val members: List<HouseHoldMember>?,
    val pregnancyInfos: List<PregnancyDetail>?,
    val followUps: List<FollowUp>?,
    val followUpCriteria: FollowUpCriteria?,
    val householdMemberLinks: List<LinkHouseholdMember>?,
    val communityProfiles: List<JsonObject>?,
    val lastSyncTime: String,
)
