package org.medtroniclabs.uhis.data.offlinesync.model

import com.google.gson.JsonObject
import org.medtroniclabs.uhis.db.entity.FollowUp
import org.medtroniclabs.uhis.db.entity.LinkHouseholdMember
import org.medtroniclabs.uhis.db.entity.PregnancyDetail

data class ResponseInitialDownload(
    val households: List<HouseHold>?,
    val members: List<HouseHoldMember>?,
    val pregnancyInfos: List<PregnancyDetail>?,
    val followUps: List<FollowUp>?,
    val followUpCriteria: FollowUpCriteria?,
    val householdMemberLinks: List<LinkHouseholdMember>?,
    val communityProfiles: List<JsonObject>?,
    val treatmentDetails: List<TreatmentDetails>?,
    val rxBuddies: List<ResponseRxBuddy>?,
    val lastSyncTime: String,
)
