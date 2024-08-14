package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.getStringOrEmptyString
import com.medtroniclabs.spice.common.DefinedParams.CHIEF_DOM_CODE_LENGTH
import com.medtroniclabs.spice.common.DefinedParams.PATIENT_NUMBER_LENGTH
import com.medtroniclabs.spice.common.DefinedParams.VILLAGE_CODE_LENGTH
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.db.entity.PregnancyDetail
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.mappingkey.MemberRegistration
import com.medtroniclabs.spice.mappingkey.MemberRegistration.otherFamilyMember
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class HouseholdMemberRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {
    suspend fun registerMember(
        map: HashMap<String, Any>,
        householdId: Long,
        entity: HouseholdMemberEntity? = null,
        parentId: String? = null
    ): Long? {
        val memberEntity = createOrUpdateHouseHoldMemberEntity(map, householdId, entity, parentId)
        if (memberEntity.patientId == null) {
            return  null
        }
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
        entity: HouseholdMemberEntity? = null,
        parentId: String?
    ): HouseholdMemberEntity {
        val householdMemberEntity = entity ?: HouseholdMemberEntity()

        val name = map[MemberRegistration.name]
        householdMemberEntity.name = getStringOrEmptyString(name)

        parentId?.let {
            householdMemberEntity.parentId = it
        }

        val phoneNumber = map[MemberRegistration.phoneNumber]
        householdMemberEntity.phoneNumber = getStringOrEmptyString(phoneNumber)

        val phoneNumberCategory = map[MemberRegistration.phoneNumberCategory]
        householdMemberEntity.phoneNumberCategory =
            getStringOrEmptyString(phoneNumberCategory)

        val dateOfBirth = map[MemberRegistration.dateOfBirth]
        householdMemberEntity.dateOfBirth = getStringOrEmptyString(dateOfBirth)

        val gender = map[MemberRegistration.gender]
        householdMemberEntity.gender = getStringOrEmptyString(gender)

        val householdHeadRelationship = map[MemberRegistration.householdHeadRelationship]
        val otherHouseholdRelationship =
            if (map.containsKey(otherFamilyMember)) map[otherFamilyMember] else null
        householdMemberEntity.householdHeadRelationship =
            householdRelationshipStatus(householdHeadRelationship, otherHouseholdRelationship)

        val isPregnantOrNot = map[MemberRegistration.isPregnant]
        householdMemberEntity.isPregnant =
            isPregnantOrNot?.let { CommonUtils.getIsBooleanFromString(isPregnantOrNot) }

        if (entity == null) {
            val householdDetails = roomHelper.getHouseHoldDetailsById(householdId)
            householdMemberEntity.householdId = householdId
            householdMemberEntity.villageId = householdDetails.villageId
            householdMemberEntity.patientId = getNextPatientId(householdDetails.villageId)
        } else {
            householdMemberEntity.updatedAt = System.currentTimeMillis()
            householdMemberEntity.sync_status = OfflineSyncStatus.NotSynced
        }

        return householdMemberEntity
    }

    private fun householdRelationshipStatus(
        householdHeadRelationship: Any?,
        otherHouseholdRelationship: Any?
    ): String {
        return if (otherHouseholdRelationship != null) {
            "${getStringOrEmptyString(householdHeadRelationship)}-${
                getStringOrEmptyString(
                    otherHouseholdRelationship
                )
            }"
        } else {
            getStringOrEmptyString(householdHeadRelationship)
        }
    }

    private suspend fun getNextPatientId(villageId: Long): String? {
        val villageDetail = roomHelper.getVillageByID(villageId)
        if (villageDetail.chiefdomCode.isNullOrBlank()) {
           return null
        }
        val chiefDomCode = villageDetail.chiefdomCode.padStart(CHIEF_DOM_CODE_LENGTH, '0')
        val villageCode = villageDetail.villagecode.padStart(VILLAGE_CODE_LENGTH, '0')
        val chwUserId = SecuredPreference.getUserId().toString()
        val startIndex = chiefDomCode.length + villageCode.length + chwUserId.length

        val lastPatientId =
            roomHelper.getLastPatientId("$chiefDomCode$villageCode$chwUserId%")?.let { it.substring(startIndex, it.length) }
                ?.toInt() ?: 0
        val nextPatientId = (lastPatientId + 1).toString().padStart(PATIENT_NUMBER_LENGTH, '0')
        return "$chiefDomCode$villageCode$chwUserId$nextPatientId"
    }

    suspend fun getMemberDetailsByID(
        memberId: Long,
    ): Resource<HouseholdMemberEntity> {
        return try {
            val memberEntity = roomHelper.getMemberDetailsByID(memberId)
            Resource(state = ResourceState.SUCCESS, data = memberEntity)
        } catch (e: Exception) {
            Resource(state = ResourceState.SUCCESS)
        }
    }

    suspend fun getMemberDetailsByParentId(
        memberId: String,
    ): Resource<List<HouseholdMemberEntity>> {
        return try {
            val memberEntity = roomHelper.getMemberDetailsByParentId(memberId)
            Resource(state = ResourceState.SUCCESS, data = memberEntity)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getMemberDetailsByPatientId(
        patientId: String,
    ): Resource<HouseholdMemberEntity> {
        return try {
            val memberEntity = roomHelper.getMemberDetailsByPatientId(patientId)
            Resource(state = ResourceState.SUCCESS, data = memberEntity)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getAssessmentMemberDetails(
        id: Long
    ): Resource<AssessmentMemberDetails> {
        return try {
            val memberEntity = roomHelper.getAssessmentMemberDetails(id)
            Resource(state = ResourceState.SUCCESS, data = memberEntity)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    private suspend fun getMemberCountPerHouseHold(householdId: Long): Int {
        return roomHelper.getMemberCountPerHouseHold(householdId)
    }

    private suspend fun updateHeadCount(householdId: Long, newNoOfPeople: Int) {
        return roomHelper.updateHeadCount(householdId, newNoOfPeople)
    }

    suspend fun getPatientVisitCountByType(
        type: String,
        patientId: String
    ): MemberClinicalEntity? {
        return roomHelper.getPatientVisitCountByType(type, patientId)
    }

    suspend fun savePatientVisitCountByType(memberClinicalEntity: MemberClinicalEntity) {
        roomHelper.savePatientVisitCountByType(memberClinicalEntity)
    }

    suspend fun getPregnancyDetailByPatientId(patientId: String): PregnancyDetail? {
        return roomHelper.getPregnancyDetailByPatientId(patientId)
    }

    suspend fun savePregnancyDetail(pregnancyDetail: PregnancyDetail): Long {
        return roomHelper.savePregnancyDetail(pregnancyDetail)
    }

}