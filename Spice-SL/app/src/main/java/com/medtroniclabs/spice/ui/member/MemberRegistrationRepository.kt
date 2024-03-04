package com.medtroniclabs.spice.ui.member

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.calculateAgeString
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.LastCreatedAtAndPatientId
import com.medtroniclabs.spice.data.VillageInfo
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.mappingkey.MemberRegistration
import com.medtroniclabs.spice.network.resource.Resource
import javax.inject.Inject

class MemberRegistrationRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {
    suspend fun registerMember(
        map: HashMap<String, Any>,
        householdId: Long,
        memberRegistrationLiveData: MutableLiveData<Resource<Long>>,
        memberDetails: MutableLiveData<Resource<HouseholdMemberEntity>>
    ) {
        try {
            memberRegistrationLiveData.postLoading()
            val householdDetails = roomHelper.getHouseHoldDetailsById(householdId)
            val villageId = householdDetails.villageId
            val noOfPerson = householdDetails.noOfPeople
            val memberRegistrationEntity = composeResultMapEntity(map, householdId, memberDetails,villageId)

            val rowId = roomHelper.registerMember(memberRegistrationEntity)
            if (memberDetails.value == null) {
                val getCountOfHouseHold = getMemberCountPerHouseHold(householdId)
                if (getCountOfHouseHold > noOfPerson) {
                    updateHeadCount(householdId, getCountOfHouseHold)
                }
            }
            memberRegistrationLiveData.postSuccess(rowId)
        } catch (e: Exception) {
            memberRegistrationLiveData.postError()
        }
    }

    private suspend fun composeResultMapEntity(
        map: HashMap<String, Any>,
        householdId: Long,
        memberDetails: MutableLiveData<Resource<HouseholdMemberEntity>>,
        villageId: Long
    ): HouseholdMemberEntity {
        val name = map[MemberRegistration.name]
        val phoneNumber = map[MemberRegistration.phoneNumber]
        val phoneNumberCategory = map[MemberRegistration.phoneNumberCategory]
        val dateOfBirth = map[MemberRegistration.dateOfBirth]
        val age = calculateAgeString(map)
        val gender = map[MemberRegistration.gender]
        val householdHeadRelationship = map[MemberRegistration.householdHeadRelationship]

        return HouseholdMemberEntity(
            id = memberDetails.value?.data?.id ?: 0,
            name = CommonUtils.getStringOrEmptyString(name),
            phoneNumber = CommonUtils.getStringOrEmptyString(phoneNumber),
            phoneNumberCategory = CommonUtils.getStringOrEmptyString(phoneNumberCategory),
            dateOfBirth = CommonUtils.getStringOrEmptyString(dateOfBirth),
            age = age,
            gender = CommonUtils.getStringOrEmptyString(gender),
            householdHeadRelationship = CommonUtils.getStringOrEmptyString(
                householdHeadRelationship
            ),
            householdId = householdId,
            patientId = memberDetails.value?.data?.patientId ?: getChiefDomAndVillageCodeByVillageId(villageId).let { villageInfo ->
                generatePatientId(
                    villageInfo.chiefdomId,
                    getLastPatientId(),
                    villageInfo.code
                )
            },
            createdAt = memberDetails.value?.data?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun generatePatientId(chiefDomId: Long, lastCreatedAtAndPatientId: LastCreatedAtAndPatientId, villageCode: String): String {
        val startIndex = chiefDomId.toString().length + villageCode.length + SecuredPreference.getUserId().toString().length
        val lastPatientId = lastCreatedAtAndPatientId.lastPatientId?.substring(startIndex,lastCreatedAtAndPatientId.lastPatientId.length)?.toInt() ?: 0
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

    suspend fun getMemberCountPerHouseHold(householdId: Long): Int {
        return roomHelper.getMemberCountPerHouseHold(householdId)
    }
    suspend fun updateHeadCount(householdId: Long, newNoOfPeople: Int) {
        return roomHelper.updateHeadCount(householdId, newNoOfPeople)
    }

    suspend fun getLastPatientId(): LastCreatedAtAndPatientId {
        return roomHelper.getLastPatientId()
    }

    suspend fun getChiefDomAndVillageCodeByVillageId(villageId: Long): VillageInfo {
        return roomHelper.getChiefDomAndVillageCodeByVillageId(villageId)
    }
}