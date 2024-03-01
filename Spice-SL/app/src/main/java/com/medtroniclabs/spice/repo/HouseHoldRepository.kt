package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.data.VillageInfo
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class HouseHoldRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {
    suspend fun registerHousehold(householdEntity: HouseholdEntity): Long =
        roomHelper.saveHouseHoldEntry(householdEntity)

    suspend fun getHouseHoldList(): ArrayList<HouseHoldEntityWithMemberCount> =
        roomHelper.getHouseHoldList()

    suspend fun getLastHouseholdNo(villageId: Long): Long? =
        roomHelper.getLastHouseholdNo(villageId)

    suspend fun searchByHouseholdNameOrNo(searchTerm: String) =
        roomHelper.searchByHouseholdNameOrNo(searchTerm)

    suspend fun getHouseHoldDetailsById(houseHoldId: Long) =
        roomHelper.getHouseHoldDetailsById(houseHoldId)

    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): ArrayList<HouseholdMemberEntity> =
        roomHelper.getAllHouseHoldMemberList(houseHoldId)

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

    suspend fun updateHousehold(
        map: HashMap<String, Any>,
        houseHoldDetailLiveData: MutableLiveData<Resource<HouseholdEntity>>,
        householdId: Long
    ) =
        roomHelper.updateHousehold(
            composeHouseholdEntityDetails(
                map,
                houseHoldDetailLiveData,
                householdId
            )
        )

    suspend fun composeHouseholdEntityDetails(
        map: HashMap<String, Any>,
        houseHoldDetailLiveData: MutableLiveData<Resource<HouseholdEntity>>,
        householdId: Long
    ): HouseholdEntity {
        val householdName = map[HouseHoldRegistration.householdName]
        val headPhoneNumber = map[HouseHoldRegistration.headPhoneNumber]
        val landmark = map[HouseHoldRegistration.landmark]
        val villageID = map[HouseHoldRegistration.villageId]
        val noOfPeople = map[HouseHoldRegistration.noOfPeople]
        val isOwnedAnImprovedLatrine = map[HouseHoldRegistration.isOwnedAnImprovedLatrine]
        val isOwnedHandWashingFacilityWithSoap =
            map[HouseHoldRegistration.isOwnedHandWashingFacilityWithSoap]
        val isOwnedATreatedBedNet = map[HouseHoldRegistration.isOwnedATreatedBedNet]
        val bedNetCount = map[HouseHoldRegistration.bedNetCount]
        val villageLongID = CommonUtils.getLongOrNull(villageID) ?: 0
        val lastHouseHoldNo = getLastHouseholdNo(villageLongID) ?: 0
        val headCount = getMemberCountPerHouseHold(householdId)
        return HouseholdEntity(
            id = getPrimaryId(householdId, houseHoldDetailLiveData),
            householdNo = getRegisteredHouseholdNo(
                lastHouseHoldNo,
                householdId,
                houseHoldDetailLiveData
            ),
            name = CommonUtils.getStringOrEmptyString(householdName),
            villageId = villageLongID,
            landmark = CommonUtils.getStringOrEmptyString(landmark),
            headPhoneNumber = CommonUtils.getStringOrEmptyString(headPhoneNumber),
            noOfPeople = checkHeadCountOfHouseHold(
                CommonUtils.getIntegerOrNull(noOfPeople) ?: 0,
                headCount
            ),
            isOwnedAnImprovedLatrine = CommonUtils.getIsBooleanFromString(isOwnedAnImprovedLatrine),
            isOwnedHandWashingFacilityWithSoap = CommonUtils.getIsBooleanFromString(
                isOwnedHandWashingFacilityWithSoap
            ),
            isOwnedATreatedBedNet = CommonUtils.getIsBooleanFromString(isOwnedATreatedBedNet),
            bedNetCount = CommonUtils.getIntegerOrNull(bedNetCount)
        )
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
        householdId: Long,
        houseHoldDetailLiveData: MutableLiveData<Resource<HouseholdEntity>>
    ): Long {
        return if (householdId != -1L) {
            houseHoldDetailLiveData.value?.data?.householdNo ?: 1
        } else lastHouseHoldNo + 1
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
}