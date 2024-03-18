package com.medtroniclabs.spice.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.VillageInfo
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
import com.medtroniclabs.spice.offlinesync.model.HouseHold
import com.medtroniclabs.spice.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.offlinesync.model.SyncResponse
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
        formLayoutsLiveData: MutableLiveData<Resource<String>>
    ) {
        try {
            formLayoutsLiveData.postLoading()
            val response = roomHelper.getFormData(formType)
            formLayoutsLiveData.postSuccess(response)
        } catch (e: Exception) {
            formLayoutsLiveData.postError()
        }
    }

    suspend fun getChiefDomAndVillageCodeByVillageId(id: Long): VillageInfo {
       return roomHelper.getChiefDomAndVillageCodeByVillageId(id)
    }

    suspend fun getAllVillagesName(villageListResponse: MutableLiveData<Resource<List<VillageEntity>>>) {
        val response = roomHelper.getAllVillageName()
        villageListResponse.postSuccess(response)
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
            householdEntity.isSynced = false

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
    private fun getRegisteredHouseholdNo(
        lastHouseHoldNo: Long,
        houseHoldEntity: HouseholdEntity? = null
    ): Long {
        return houseHoldEntity?.householdNo ?: lastHouseHoldNo + 1
    }

    private fun getPrimaryId(
        householdId: Long,
        houseHoldDetailLiveData: MutableLiveData<Resource<HouseholdEntity>>
    ): Long {
        return if (householdId != -1L) {
            houseHoldDetailLiveData.value?.data?.id ?: -1
        } else 0
    }

    suspend fun getUserVillages(
        villageListResponse: MutableLiveData<Resource<LocalSpinnerResponse>>,
        tag: String
    ) {
        try {
            villageListResponse.postLoading()
            val response = roomHelper.getUserVillages()
            villageListResponse.postValue(
                Resource(
                    ResourceState.SUCCESS,
                    LocalSpinnerResponse(tag, response)
                )
            )
        } catch (_: Exception) {
            villageListResponse.postError()
        }
    }

    suspend fun getVillageByID(villageId: Long, villageListResponse: MutableLiveData<Resource<VillageEntity>>) {
        val response = roomHelper.getVillageByID(villageId)
        villageListResponse.postSuccess(response)
    }

    suspend fun getMemberCountPerHouseHold(householdId: Long): Int {
        return roomHelper.getMemberCountPerHouseHold(householdId)
    }

    suspend fun getAllUnSyncedHouseHolds(): List<HouseHold> {
        return roomHelper.getAllUnSyncedHouseHolds()
    }

    suspend fun getAllUnSyncedMembers(householdId: Long): List<HouseHoldMember> {
        return roomHelper.getAllUnSyncedHouseHoldMembers(householdId)
    }

    suspend fun getOtherHouseholdMembers(ids: List<Long>): List<HouseHoldMember> {
        return roomHelper.getOtherHouseholdMembers(ids)
    }

    suspend fun postOfflineHouseHolds(map: Map<String,Any>): Response<SyncResponse> {
        return apiHelper.postOfflineSync(map)
    }

    suspend fun getSyncStatus(request: RequestGetSyncStatus): Response<SyncResponse> {
        return apiHelper.getOfflineSyncStatus(request)
    }

    suspend fun updateFhirId(tableName: String, id: String, fhirId: String) {
        roomHelper.updateFhirId(tableName, id, fhirId)
    }
}