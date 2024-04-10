package com.medtroniclabs.spice.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.VillageInfo
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.model.SyncEntityList
import com.medtroniclabs.spice.data.offlinesync.model.SyncResponse
import com.medtroniclabs.spice.data.resource.RequestAllEntities
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
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
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
        val response = roomHelper.getAllVillageEntity()
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

    suspend fun getHouseholdAndMembers(liveData: MutableLiveData<Resource<Boolean>>) {
        liveData.postLoading()
        val villageNameId = mutableMapOf<String, Long>()
        roomHelper.getAllVillageEntity().forEach {
            villageNameId[it.name] = it.id
        }

        try {
            val syncedResponse = getSyncedEntities(villageNameId.values.toList())
            val unSyncedResponse = getUnSyncedEntities()
            if (syncedResponse.isSuccessful && unSyncedResponse.isSuccessful) {
                roomHelper.deleteAllHouseholds()
                roomHelper.deleteAllHouseholdMembers()
                // Insert Synced Entities
                insertHouseholdAndMembers(
                    syncedResponse.body()?.entityList,
                    villageNameId,
                    OfflineSyncStatus.Success
                )

                // Insert UnSynced Entities
                val householdList =
                    unSyncedResponse.body()?.entityList?.filter { it.type == HOUSEHOLD }
                val hhMap = insertHouseholds(householdList, villageNameId)

                val householdMemberList =
                    unSyncedResponse.body()?.entityList?.filter { it.type == HOUSEHOLD_MEMBER }
                insertHouseholdMembers(householdMemberList, hhMap)

                liveData.postSuccess(true)
            } else {
                liveData.postError("Something went wrong")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postError(e.message)
        }
    }

    private suspend fun insertHouseholdAndMembers(households: List<HouseHold>?, villageNameId : Map<String,Long>, status: OfflineSyncStatus) {
        households?.forEach { household ->
            //Inserting Household
            val householdEntity = household.toHouseholdEntity(villageNameId, status)
            val hhId = roomHelper.saveHouseHoldEntry(householdEntity)

            //Inserting HouseholdMember
            household.householdMembers.forEach { householdMember ->
                val householdMemberEntity = householdMember.toHouseholdMemberEntity(hhId, status)
                roomHelper.registerMember(householdMemberEntity)
            }
        }
    }

    private suspend fun insertHouseholds(households: List<SyncEntityList>?, villageNameId : Map<String,Long>): Map<String, HouseHold> {
        // Response apiReferenceId, Household
        val hhMap = mutableMapOf<String, HouseHold>()
        households?.forEach { entity ->
            Gson().fromJson(entity.data, HouseHold::class.java)?.let { houseHold ->
                val apiRefId = houseHold.referenceId
                var dbHHId: Long?
                if (houseHold.id != null) { // Fhir id is not null - Success
                    dbHHId = roomHelper.getHouseholdIdByFhirId(houseHold.id)
                    if (dbHHId != null) { // Update Flow
                        roomHelper.updateHousehold(houseHold.toHouseholdEntity(villageNameId, OfflineSyncStatus.Success, dbHHId))
                    } else { // Insert Flow
                        dbHHId = roomHelper.saveHouseHoldEntry(houseHold.toHouseholdEntity(villageNameId, OfflineSyncStatus.Success))
                    }
                } else { // Fhir id is null - Failed
                    dbHHId = roomHelper.saveHouseHoldEntry(houseHold.toHouseholdEntity(villageNameId, OfflineSyncStatus.Failed))
                }

                houseHold.referenceId = dbHHId.toString()
                hhMap[apiRefId!!] = houseHold
            }
        }
        return hhMap
    }

    private suspend fun insertHouseholdMembers(householdMemberList: List<SyncEntityList>?, hhMap: Map<String, HouseHold>) {
        householdMemberList?.forEach { entity ->
            Gson().fromJson(entity.data, HouseHoldMember::class.java)?.let { member ->
                val dbHHId = roomHelper.getHouseholdIdByFhirId(member.householdId) ?: hhMap[member.householdReferenceId]?.referenceId?.toLong()
                if (dbHHId != null) { // HouseholdId found in local
                    if (member.id != null) { //  Fhir id is not null - Success
                        val dbHHMId = roomHelper.getHouseholdMemberIdByFhirId(member.id)
                        if (dbHHMId != null) { // Update Flow
                            roomHelper.registerMember(member.toHouseholdMemberEntity(dbHHId, OfflineSyncStatus.Success, dbHHMId))
                        } else { // Insert Flow
                            roomHelper.registerMember(member.toHouseholdMemberEntity(dbHHId, OfflineSyncStatus.Success))
                        }
                    } else { // Fhir id is null - Failed
                        roomHelper.registerMember(member.toHouseholdMemberEntity(dbHHId, OfflineSyncStatus.Failed))
                    }
                }
            }
        }
    }

    private suspend fun getSyncedEntities(villageList: List<Long>): Response<APIResponse<List<HouseHold>>> {
        // Getting village name only. For mapping I have used following code
        val request = RequestAllEntities(villageList)
        return apiHelper.getHouseholdAndMembers(request)
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