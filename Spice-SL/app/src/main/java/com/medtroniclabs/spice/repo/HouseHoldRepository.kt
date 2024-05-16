package com.medtroniclabs.spice.repo

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.model.SyncResponse
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.EntitiesName.HOUSEHOLD
import com.medtroniclabs.spice.db.entity.EntitiesName.HOUSEHOLD_MEMBER
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.db.response.HouseholdMemberCount
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import retrofit2.Response
import javax.inject.Inject

class HouseHoldRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    suspend fun getLastHouseholdNo(villageId: Long): Long? =
        roomHelper.getLastHouseholdNo(villageId)

    suspend fun getHouseHoldDetailsById(houseHoldId: Long) =
        roomHelper.getHouseHoldDetailsById(houseHoldId)

    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): ArrayList<HouseholdMemberEntity> =
        roomHelper.getAllHouseHoldMemberList(houseHoldId)

    fun getMemberCountInHouseholdLiveData(houseHoldId: Long): LiveData<HouseholdMemberCount> {
        return roomHelper.getMemberCountInHouseholdLiveData(houseHoldId)
    }

    fun getFilteredHouseholdsLiveData(searchTerm: String, villageIds: List<Long>, status: String): LiveData<List<HouseHoldEntityWithMemberCount>> {
        return roomHelper.getFilteredHouseholdsLiveData(searchTerm, villageIds, status)
    }

    suspend fun getFormData(
        formType: String,
    ): Resource<String> {
        return try {
            val response = roomHelper.getFormData(formType)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }


    suspend fun getAllVillagesName(): Resource<List<VillageEntity>> {
        val response = roomHelper.getAllVillageEntity()
        return Resource(state = ResourceState.SUCCESS, data = response)
    }

    suspend fun createOrUpdateHouseHoldEntity(map: HashMap<String, Any>, entity: HouseholdEntity? = null): HouseholdEntity {
        val householdEntity = entity ?: HouseholdEntity()

        val householdName = map[HouseHoldRegistration.householdName]
        householdEntity.name = CommonUtils.getStringOrEmptyString(householdName)

        val headPhoneNumber = map[HouseHoldRegistration.headPhoneNumber]
        householdEntity.headPhoneNumber = CommonUtils.getStringOrEmptyString(headPhoneNumber)

        val landmark = map[HouseHoldRegistration.landmark]
        householdEntity.landmark = CommonUtils.getStringOrEmptyString(landmark)

        val villageID = map[HouseHoldRegistration.villageId]
        val villageLongID = CommonUtils.getLongOrNull(villageID) ?: 0
        householdEntity.villageId = villageLongID

        val isOwnedAnImprovedLatrine = map[HouseHoldRegistration.isOwnedAnImprovedLatrine]
        householdEntity.isOwnedAnImprovedLatrine = CommonUtils.getIsBooleanFromString(isOwnedAnImprovedLatrine)

        val isOwnedHandWashingFacilityWithSoap =
            map[HouseHoldRegistration.isOwnedHandWashingFacilityWithSoap]
        householdEntity.isOwnedHandWashingFacilityWithSoap = CommonUtils.getIsBooleanFromString(
            isOwnedHandWashingFacilityWithSoap
        )

        val isOwnedATreatedBedNet = map[HouseHoldRegistration.isOwnedATreatedBedNet]
        householdEntity.isOwnedATreatedBedNet = CommonUtils.getIsBooleanFromString(isOwnedATreatedBedNet)

        val bedNetCount = map[HouseHoldRegistration.bedNetCount]
        householdEntity.bedNetCount = CommonUtils.getIntegerOrNull(bedNetCount)

        val lastHouseHoldNo = getLastHouseholdNo(villageLongID) ?: 0

        if (entity != null) {
            householdEntity.updatedAt = System.currentTimeMillis()
            householdEntity.sync_status = OfflineSyncStatus.NotSynced

            val noOfPeople = map[HouseHoldRegistration.noOfPeople]
            householdEntity.noOfPeople = checkHeadCountOfHouseHold(CommonUtils.getIntegerOrNull(noOfPeople) ?: 0, getMemberCountPerHouseHold(entity.id))
        } else {
            householdEntity.householdNo = lastHouseHoldNo + 1
            val noOfPeople = map[HouseHoldRegistration.noOfPeople]
            householdEntity.noOfPeople = CommonUtils.getIntegerOrNull(noOfPeople) ?: 0
        }

        return householdEntity
    }

    suspend fun insertHouseHoldEntity(householdEntity: HouseholdEntity): Long {
        return roomHelper.saveHouseHoldEntry(householdEntity)
    }

    suspend fun updateHouseHoldEntity(householdEntity: HouseholdEntity) {
        roomHelper.updateHousehold(householdEntity)
    }

    private fun checkHeadCountOfHouseHold(
        givenHeadCount: Int,
        memberCount: Int
    ): Int {
        return if (memberCount > givenHeadCount) {
            memberCount
        } else {
            givenHeadCount
        }
    }

    suspend fun getUserVillages(
        tag: String
    ): Resource<LocalSpinnerResponse> {
        return try {
            val response = roomHelper.getUserVillages()
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getVillageByID(villageId: Long): Resource<VillageEntity> {
        val response = roomHelper.getVillageByID(villageId)
        return Resource(state = ResourceState.SUCCESS, data = response)
    }

    suspend fun getMemberCountPerHouseHold(householdId: Long): Int {
        return roomHelper.getMemberCountPerHouseHold(householdId)
    }

    private suspend fun insertHouseholdMembers(householdMembers: List<HouseHoldMember>?, hhIdMap: Map<String, Long>) {
        householdMembers?.forEach { member ->
            hhIdMap[member.householdId]?.let {
                roomHelper.registerMember(
                    member.toHouseholdMemberEntity(
                        it,
                        OfflineSyncStatus.Success
                    )
                )
            }
        }
    }

    private suspend fun getUnSyncedEntities(): Response<SyncResponse> {
        val req = RequestGetSyncStatus(
            userId = SecuredPreference.getUserId(),
            dataRequired = true,
            statuses = listOf(OfflineSyncStatus.InProgress.name, OfflineSyncStatus.Failed.name),
            types = listOf(HOUSEHOLD, HOUSEHOLD_MEMBER)
        )

        return apiHelper.getOfflineSyncStatus(req)
    }

    suspend fun getUnSyncedHouseholdCount(): Int {
        return roomHelper.getUnSyncedHouseholdCount()
    }

    suspend fun getUnSyncedHouseholdMemberCount(): Int {
        return roomHelper.getUnSyncedHouseholdMemberCount()
    }
}