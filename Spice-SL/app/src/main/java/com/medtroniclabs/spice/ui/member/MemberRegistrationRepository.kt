package com.medtroniclabs.spice.ui.member

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.calculateAgeString
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
        memberDetails: MutableLiveData<Resource<HouseholdMemberEntity>>,
        noOfPerson: Int
    ) {
        try {
            memberRegistrationLiveData.postLoading()
            val memberRegistrationEntity = composeResultMapEntity(map, householdId, memberDetails)
            val rowId = roomHelper.registerMember(memberRegistrationEntity)
            if (memberDetails.value == null){
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

    private fun composeResultMapEntity(
        map: HashMap<String, Any>,
        householdId: Long,
        memberDetails: MutableLiveData<Resource<HouseholdMemberEntity>>
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
            householdId = householdId
        )
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
}