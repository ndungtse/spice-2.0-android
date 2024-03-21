package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.getStringOrEmptyString
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.LastCreatedAtAndPatientId
import com.medtroniclabs.spice.data.VillageInfo
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.mappingkey.MemberRegistration
import com.medtroniclabs.spice.mappingkey.MemberRegistration.otherFamilyMember
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.offlinesync.utils.OfflineSyncStatus
import javax.inject.Inject

class MemberRegistrationRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {
    suspend fun registerMember(
        map: HashMap<String, Any>,
        householdId: Long,
        entity: HouseholdMemberEntity? = null
    ): Long {
        val memberEntity = createOrUpdateHouseHoldMemberEntity(map, householdId, entity)
        val memberId = roomHelper.registerMember(memberEntity)

        //Update Member count in household only in insert case
        if (entity == null) {
            val memberAddedForHouseHold = getMemberCountPerHouseHold(householdId)
            val memberMentionedInHouseHold =
                roomHelper.getHouseHoldDetailsById(householdId).noOfPeople
            if (memberAddedForHouseHold > memberMentionedInHouseHold) {
                updateHeadCount(householdId, memberAddedForHouseHold)
            }
        }

        return memberId
    }

    private suspend fun createOrUpdateHouseHoldMemberEntity(
        map: HashMap<String, Any>,
        householdId: Long,
        entity: HouseholdMemberEntity? = null
    ): HouseholdMemberEntity {
        val householdMemberEntity = entity ?: HouseholdMemberEntity()

        val name = map[MemberRegistration.name]
        householdMemberEntity.name = CommonUtils.getStringOrEmptyString(name)

        val phoneNumber = map[MemberRegistration.phoneNumber]
        householdMemberEntity.phoneNumber = CommonUtils.getStringOrEmptyString(phoneNumber)

        val phoneNumberCategory = map[MemberRegistration.phoneNumberCategory]
        householdMemberEntity.phoneNumberCategory =
            CommonUtils.getStringOrEmptyString(phoneNumberCategory)

        val dateOfBirth = map[MemberRegistration.dateOfBirth]
        householdMemberEntity.dateOfBirth = CommonUtils.getStringOrEmptyString(dateOfBirth)

        val gender = map[MemberRegistration.gender]
        householdMemberEntity.gender = CommonUtils.getStringOrEmptyString(gender)

        val householdHeadRelationship = map[MemberRegistration.householdHeadRelationship]
        val otherHouseholdRelationship = if(map.containsKey(otherFamilyMember)) map [otherFamilyMember] else null
        householdMemberEntity.householdHeadRelationship = householdRelationshipStatus(householdHeadRelationship, otherHouseholdRelationship)

        val isPregnantOrNot = map[MemberRegistration.isPregnant]
        householdMemberEntity.isPregnant = isPregnantOrNot?.let {  CommonUtils.getIsBooleanFromString(isPregnantOrNot) }

        if (entity == null) {
            val householdDetails = roomHelper.getHouseHoldDetailsById(householdId)
            householdMemberEntity.householdId = householdId
            householdMemberEntity.patientId =
                getChiefDomAndVillageCodeByVillageId(householdDetails.villageId).let { villageInfo ->
                    generatePatientId(
                        villageInfo.chiefdomId,
                        getLastPatientId(),
                        villageInfo.code
                    )
                }
        } else {
            householdMemberEntity.updatedAt = System.currentTimeMillis()
            householdMemberEntity.sync_status = OfflineSyncStatus.NotSynced
        }

        return householdMemberEntity
    }

    private fun householdRelationshipStatus(householdHeadRelationship: Any?, otherHouseholdRelationship: Any?): String {
        return if (otherHouseholdRelationship != null){
             "${getStringOrEmptyString(householdHeadRelationship)}-${getStringOrEmptyString(otherHouseholdRelationship)}"
        } else {
            getStringOrEmptyString(householdHeadRelationship)
        }
    }

    private fun generatePatientId(
        chiefDomId: Long,
        lastCreatedAtAndPatientId: LastCreatedAtAndPatientId?,
        villageCode: String
    ): String {
        val startIndex =
            chiefDomId.toString().length + villageCode.length + SecuredPreference.getUserId()
                .toString().length
        val lastPatientId = lastCreatedAtAndPatientId?.lastPatientId?.substring(
            startIndex,
            lastCreatedAtAndPatientId.lastPatientId.length
        )?.toInt() ?: 0
        val numericPart = (lastPatientId + 1).toString().padStart(4, '0')
        return "$chiefDomId$villageCode${SecuredPreference.getUserId()}$numericPart"
    }

    suspend fun getMemberDetailsByID(
        memberId: Long,
        memberDetailsLiveData: MutableLiveData<Resource<HouseholdMemberEntity>>
    ) {
        try {
            memberDetailsLiveData.postLoading()
            val memberEntity = roomHelper.getMemberDetailsByID(memberId)
            memberDetailsLiveData.postSuccess(memberEntity)
        } catch (e: Exception) {
            memberDetailsLiveData.postError()
        }
    }

    private suspend fun getMemberCountPerHouseHold(householdId: Long): Int {
        return roomHelper.getMemberCountPerHouseHold(householdId)
    }

    private suspend fun updateHeadCount(householdId: Long, newNoOfPeople: Int) {
        return roomHelper.updateHeadCount(householdId, newNoOfPeople)
    }

    private suspend fun getLastPatientId(): LastCreatedAtAndPatientId? {
        return roomHelper.getLastPatientId()
    }

    private suspend fun getChiefDomAndVillageCodeByVillageId(villageId: Long): VillageInfo {
        return roomHelper.getChiefDomAndVillageCodeByVillageId(villageId)
    }
}