package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.RxBuddyDetails
import com.medtroniclabs.spice.db.entity.RxBuddyFollowUpEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class RxBuddyRepository @Inject constructor(
    private var roomHelper: RoomHelper
){
    suspend fun insertRxBuddyDetails(
        rxBuddyId:Long?,
        patientMemberId:String,
        memberId:String?,
        name:String?,
        phoneNumber:String?,
        relationship:String,
        isMonitorSheetProvider:Boolean
    ):Resource<Long>{
      return try {
          val rxBuddy = RxBuddyDetails(
              rxBuddyId = rxBuddyId,
              patientMemberId = patientMemberId,
              memberId = memberId,
              name = name,
              phoneNumber = phoneNumber,
              relationship = relationship,
              isMonitorSheetProvider = isMonitorSheetProvider
          )
          val response = roomHelper.insertRxBuddyDetails(rxBuddy)
          Resource(state = ResourceState.SUCCESS, data = response)
      }catch (e:Exception){
          Resource(state = ResourceState.ERROR, data = null)
      }
    }

    suspend fun getOtherHouseholdMembersExcludeTBPatient(householdId:Long,patientId:Long):Resource<ArrayList<Map<String,Any>>>{
       val otherHouseholdMembers = roomHelper.getOtherHouseholdExcludeTBPatient(
            householdId,
            patientId
        )

        val dropDownList = ArrayList<Map<String,Any>>()
        for((_,householdMemberEntity) in otherHouseholdMembers.withIndex()){
            dropDownList.add(
                hashMapOf<String,Any>(
                    DefinedParams.NAME to householdMemberEntity.name,
                    DefinedParams.id to householdMemberEntity.id
                )
            )
        }
        dropDownList.add(hashMapOf<String,Any>(
            DefinedParams.NAME to DefinedParams.Other,
            DefinedParams.id to 0L
        ))
        return Resource(
            state = ResourceState.SUCCESS,
            data = dropDownList
        )
    }

    suspend fun getRxBuddyDetails(patientMemberId:String):Resource<RxBuddyDetails?>{
        return try{
            val response = roomHelper.getRxBuddyDetails(patientMemberId)
            if(response != null) {
                Resource(state = ResourceState.SUCCESS, data = response)
            }else{
                Resource(state = ResourceState.ERROR, data = null)
            }
        }catch (e:Exception){
            Resource(state = ResourceState.ERROR, data = null)
        }
    }

    suspend fun insertRxBuddyFollowUp(
        rxBuddyId:Long?,
        patientMemberId:String,
        rxBuddyMonitoringSheetDate:String,
        isAnyOfSymptomsWorse:Boolean,
        isAnyOfMedicationNeeded:Boolean
    ):Resource<Long>{
        return try {
            val rxBuddyFollowUp = RxBuddyFollowUpEntity(
                rxBuddyId = rxBuddyId,
                patientMemberId = patientMemberId,
                rxBuddyMonitoringSheetDate = rxBuddyMonitoringSheetDate,
                isAnyOfSymptomsWorse = isAnyOfSymptomsWorse,
                isAnyOfMedicationNeeded = isAnyOfMedicationNeeded
            )
            val response = roomHelper.insertRxBuddyFollowUp(rxBuddyFollowUp)
            Resource(state = ResourceState.SUCCESS, data = response)
        }catch (e:Exception){
            Resource(state = ResourceState.ERROR, data = null)
        }
    }
}