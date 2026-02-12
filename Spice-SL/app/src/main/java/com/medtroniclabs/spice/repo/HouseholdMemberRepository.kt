package com.medtroniclabs.spice.repo

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.CommonUtils.getStringOrEmptyString
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.CHIEF_DOM_CODE_LENGTH
import com.medtroniclabs.spice.common.DefinedParams.PATIENT_NUMBER_LENGTH
import com.medtroniclabs.spice.common.DefinedParams.VILLAGE_CODE_LENGTH
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberFhirId
import com.medtroniclabs.spice.data.offlinesync.model.UnAssignedHouseholdMemberDetail
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant.CALL_TYPE_LINK_HHM
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.CallHistory
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.db.entity.PregnancyDetail
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.mappingkey.MemberRegistration
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.deceasedReason
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.isDeceased
import kotlinx.coroutines.launch
import javax.inject.Inject

class HouseholdMemberRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {
    suspend fun registerMember(
        map: HashMap<String, Any>,
        householdId: Long,
        entity: HouseholdMemberEntity? = null,
        parentReferenceId: Long? = null,
        initial: String? = null,
        signature: String? = null,
        isPhuWalkInFlow: Boolean? = null,
        location: Location?
    ): Long? {
        val memberEntity = createOrUpdateHouseHoldMemberEntity(map, householdId, entity, parentReferenceId, initial, signature,location)
       /* if (memberEntity.patientId == null) {
            return  null
        }*/
        val memberId = roomHelper.registerMember(memberEntity)

        // Assign same household for Parent or Child
//        if (isPhuWalkInFlow == true) {
//            val fhirIds = if (memberEntity.parentId != null) {
//                roomHelper.getUnAssignedParentFhirId(memberEntity.parentId!!)
//            } else {
//                roomHelper.getUnAssignedChildFhirIds(memberEntity.patientId!!)
//            }
//
//            if (fhirIds.isNotEmpty()) {
//                memberEntity.fhirId?.let {
//                    updateMemberAsAssigned(fhirIds, householdId, it)
//                }
//            }
//        }

        //Update Member count in household only in insert case
        if (entity == null || isPhuWalkInFlow == true) {
            val memberAddedForHouseHold = getMemberCountPerHouseHold(householdId)
            val memberMentionedInHouseHold =
                roomHelper.getHouseHoldDetailsById(householdId).noOfPeople
            if (memberAddedForHouseHold > memberMentionedInHouseHold) {
                updateHeadCount(householdId, memberAddedForHouseHold)
            }
        }

        return memberId
    }

    suspend fun updateHeadPhoneNumber(householdId: Long, map: HashMap<String, Any>) {
         val isHouseholdHead = map[MemberRegistration.isHouseholdHead]
         if (CommonUtils.getIsBooleanFromString(isHouseholdHead) == true) {
            //Updating in HouseHoldMEMBER
            roomHelper.updatePhoneNumberForHouseholdHead(
                householdId,
                map[MemberRegistration.phoneNumber]?.toString(),
                null // phoneNumberCategory parameter kept for interface compatibility
            )
        }
    }


    private suspend fun createOrUpdateHouseHoldMemberEntity(
        map: HashMap<String, Any>,
        householdId: Long,
        entity: HouseholdMemberEntity? = null,
        parentReferenceId: Long?,
        initial: String? = null,
        signature: String? = null,
        location: Location?
    ): HouseholdMemberEntity {

        val householdMemberEntity = entity ?: HouseholdMemberEntity()

        val name = map[MemberRegistration.name]
        householdMemberEntity.name = getStringOrEmptyString(name)

        parentReferenceId?.let {
            householdMemberEntity.motherReferenceId = it
        }

        val phoneNumber = map[MemberRegistration.phoneNumber]
        householdMemberEntity.phoneNumber = getStringOrEmptyString(phoneNumber)

        val dateOfBirth = map[MemberRegistration.dateOfBirth]
        householdMemberEntity.dateOfBirth = getStringOrEmptyString(dateOfBirth)

        val gender = map[MemberRegistration.gender]
        householdMemberEntity.gender = getStringOrEmptyString(gender)

        val idType = map[MemberRegistration.idType]
        householdMemberEntity.idType = getStringOrEmptyString(idType)

        val nationalId = map[MemberRegistration.nationalId]
        householdMemberEntity.nationalId = nationalId?.toString()?.takeIf { it.isNotEmpty() }

        val isHouseholdHead = map[MemberRegistration.isHouseholdHead]
        householdMemberEntity.isHouseholdHead = isHouseholdHead==true

        val householdFhirId = map[MemberRegistration.householdFhirId]
        householdMemberEntity.householdFhirId = householdFhirId?.toString()?.takeIf { it.isNotEmpty() }

        val isDeceased = map[isDeceased]
        if (isDeceased != null && isDeceased is Boolean && isDeceased){
             householdMemberEntity.isActive = false
        }

        val deceasedReason = map[deceasedReason]
        if (deceasedReason != null && deceasedReason is String){
            householdMemberEntity.deceasedReason = deceasedReason
        }

        householdMemberEntity.householdId = householdId

        if (entity == null) {
            val householdDetails = roomHelper.getHouseHoldDetailsById(householdId)
            householdMemberEntity.villageId = householdDetails.villageId
        } else {
            householdMemberEntity.updatedAt = System.currentTimeMillis()
            householdMemberEntity.sync_status = OfflineSyncStatus.NotSynced
        }
        location?.let {
            householdMemberEntity.latitude = it.latitude
            householdMemberEntity.longitude = it.longitude
        }

        return householdMemberEntity
    }


    private suspend fun getNextPatientId(villageId: Long): String? {
        val villageDetail = roomHelper.getVillageByID(villageId)
        if (villageDetail.chiefdomCode.isNullOrBlank()) {
           return null
        }
        if (villageDetail.villagecode.isNullOrBlank()) {
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

    suspend fun getMemberDetails(
        memberId: Long,
    ): HouseholdMemberEntity {
        return roomHelper.getMemberDetailsByID(memberId)
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
        hhmLocalId: Long
    ): MemberClinicalEntity? {
        return roomHelper.getPatientVisitCountByType(type, hhmLocalId)
    }

    suspend fun savePatientVisitCountByType(memberClinicalEntity: MemberClinicalEntity) {
        roomHelper.savePatientVisitCountByType(memberClinicalEntity)
    }

    suspend fun getPregnancyDetailByPatientId(hhmLocalId: Long): PregnancyDetail? {
        return roomHelper.getPregnancyDetailByPatientId(hhmLocalId)
    }

    suspend fun savePregnancyDetail(pregnancyDetail: PregnancyDetail): Long {
        return roomHelper.savePregnancyDetail(pregnancyDetail)
    }

   suspend fun updateMemberDeceasedStatus(id: Long, status: Boolean) {
        return roomHelper.updateMemberDeceasedStatus(id, status)
    }

    suspend fun getPatientIdById(id: Long): String {
        return roomHelper.getPatientIdById(id)
    }

    suspend fun changeMemberDetailsToNotSynced(id: Long) {
        roomHelper.changeMemberDetailsToNotSynced(id)
    }

    fun getUnAssignedHouseholdMember(): LiveData<List<UnAssignedHouseholdMemberDetail>> {
        return roomHelper.getUnAssignedHouseholdMembersLiveData()
    }

    suspend fun addLinkMemberCall(memberId: String, callStartTime: Long, callEndTime: Long): Long {
        val callHistory = CallHistory(
            type = CALL_TYPE_LINK_HHM,
            referenceId = memberId,
            callStartTime = callStartTime,
            callEndTime = callEndTime
        )
        return roomHelper.addLinkMemberCall(callHistory)
    }

    suspend fun updateMemberAsAssigned(memberId: String) {
        roomHelper.updateMemberAsAssigned(memberId)
    }

    suspend fun updateMemberDeceasedReason(id: Long, status: Boolean,deceasedReason: String?) {
        return roomHelper.updateMemberDeceasedReason(id, status,deceasedReason)
    }

    suspend fun getHouseholdHeadDob(householdId: Long): String {
        return roomHelper.getHouseholdHeadDob(householdId)
    }

    suspend fun updatePregnantStatus(memberId: Long, isPregnant: Boolean) {
        return roomHelper.updatePregnantStatus(memberId, isPregnant)
    }

    suspend fun getTbPatientLocalIdByHouseholdId(householdId: Long): MutableList<Long> {
        return roomHelper.getTbPatientLocalIdByHouseholdId(householdId)
    }

    suspend fun isTbPatient(memberId: String) : Boolean {
        return roomHelper.getTreatmentDetails(memberId) != null
    }

    suspend fun updateContactTracingStatus(memberId: Long, status: Int?) {
        roomHelper.updateContactTracingStatus(memberId, status)
    }

    suspend fun updateContactTracingForLinkTbPatient(tbHHMId: Long, householdId: Long) {
        roomHelper.updateContactTracingForLinkTbPatient(tbHHMId, householdId)
    }

    private suspend fun updateMemberAsAssigned(hhmFhirIds: List<HouseholdMemberFhirId>, hhId: Long, motherOrChildFhirId: String) {
        val isMotherOrChildTbPatient = isTbPatient(motherOrChildFhirId)
        hhmFhirIds.forEach { hhmFhirId ->
            val hhTbPatientIds = getTbPatientLocalIdByHouseholdId(hhId)
            val isTbPatient = isTbPatient(hhmFhirId.hhmFhirId)

            if (isTbPatient) {
                if (hhTbPatientIds.isEmpty()) {
                    updateContactTracingForLinkTbPatient(hhmFhirId.hhmId, hhId)
                }
            } else {
                if (hhTbPatientIds.isNotEmpty() || isMotherOrChildTbPatient) {
                    updateContactTracingStatus(
                        hhmFhirId.hhmId,
                        OfflineConstant.CONTACT_TRACING_YET_TO_TAKE
                    )
                } else {
                    updateContactTracingStatus(hhmFhirId.hhmId, null)
                }
            }
            roomHelper.updateHouseholdHeadAndRelationShip(listOf(hhmFhirId.hhmFhirId), hhId)
            updateMemberAsAssigned(hhmFhirId.hhmFhirId)
        }
    }

}