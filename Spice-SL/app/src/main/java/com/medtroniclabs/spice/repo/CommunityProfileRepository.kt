package com.medtroniclabs.spice.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.community.CommunityPopulationStatistics
import com.medtroniclabs.spice.data.community.CommunityProfileDetail
import com.medtroniclabs.spice.db.entity.CommunityProfile
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class CommunityProfileRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper,
) {
    fun getFilterVillageWithHouseholds(searchText: String): LiveData<List<CommunityProfileDetail>> = roomHelper.getFilterVillagesWithHouseholdsCount(searchText)

    suspend fun getCommunityStatistics(villageId: Long): Resource<CommunityPopulationStatistics> =
        try {
            val response = roomHelper.getCommunityStatistics(villageId)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR, data = null)
        }

    suspend fun getFormData(formType: String): Resource<FormResponse> =
        try {
            val response = roomHelper.getFormData(formType)
            val formFieldsType = object : TypeToken<FormResponse>() {}.type
            val formFields: FormResponse = Gson().fromJson(response, formFieldsType)
            Resource(state = ResourceState.SUCCESS, data = formFields)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getNearestHealthFacility(villageId: Long? = null): Resource<ArrayList<Map<String, Any>>> {
        val villageEntity = villageId?.let {
            roomHelper.getVillageByID(villageId)
        }
        val healthFacilityList = roomHelper.getNearestHealthFacility()

        val dropDownList = ArrayList<Map<String, Any>>()

        for ((_, healthFacilityEntity) in healthFacilityList.withIndex()) {
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to healthFacilityEntity.name,
                    DefinedParams.id to healthFacilityEntity.fhirId.toString(),
                    DefinedParams.isDefault to (
                        villageEntity?.healthFacilityId?.let { it == healthFacilityEntity.id }
                            ?: healthFacilityEntity.isDefault
                    ),
                ),
            )
        }
        return Resource(
            state = ResourceState.SUCCESS,
            data = dropDownList,
        )
    }

    suspend fun createCommunityProfile(request: HashMap<String, Any>): Resource<APIResponse<HashMap<String, Any>>> =
        try {
            val response = apiHelper.createCommunityProfile(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR, data = null)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR, data = null)
        }

    suspend fun updateCommunityProfile(request: HashMap<String, Any>): Resource<APIResponse<HashMap<String, Any>>> =
        try {
            val response = apiHelper.updateCommunityProfile(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR, data = null)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR, data = null)
        }

    /*suspend fun getCommunityProfile(villageId: Long): Resource<APIResponse<CommunityProfileDetails>> {
        val requestMap = HashMap<String, Any>()
        requestMap[DefinedParams.COMMUNITY_ID] = villageId
        return try {
            val response = apiHelper.getCommunityProfile(requestMap)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR, data = null)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR, data = null)
        }
    }*/

    suspend fun getCommunityProfileDetails(villageId: Long): Resource<com.medtroniclabs.spice.db.entity.CommunityProfile> =
        try {
            val response = roomHelper.getCommunityDetails(villageId)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR, data = null)
        }

    suspend fun insertOrUpdateCommunityProfile(
        villageId: Long,
        description: String,
        regDate: String,
        payload: String,
        communityProfileDetailLiveData: MutableLiveData<Resource<CommunityProfile>>,
    ): Resource<Long> =
        try {
            val communityProfile = roomHelper.getCommunityDetails(villageId)
            val communityProfileId = communityProfile?.id ?: 0
            val communityFhirId = communityProfile?.fhirId

            val latitude = SecuredPreference.getDouble(
                SecuredPreference.EnvironmentKey.CURRENT_LATITUDE.name,
                0.0,
            )
            val longitude = SecuredPreference.getDouble(
                SecuredPreference.EnvironmentKey.CURRENT_LONGITUDE.name,
            )
            val updateCommunityProfile = CommunityProfile(
                id = communityProfileId,
                villageId = villageId,
                communityDescription = description,
                registeredDate = regDate,
                payload = payload,
                latitude = latitude,
                longitude = longitude,
            )
            updateCommunityProfile.fhirId = communityFhirId
            communityProfileDetailLiveData.postValue(Resource(state = ResourceState.SUCCESS, data = updateCommunityProfile))
            val response = roomHelper.insertCommunityDetails(updateCommunityProfile)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR, data = null)
        }

    suspend fun updateUnSynStatus(
        villageId: Long,
        unSynStatus: String,
    ) = roomHelper.updateUnSynStatus(villageId, unSynStatus)

    suspend fun getUnSyncedCommunityProfileCount(): Int = roomHelper.getUnSyncedCommunityProfileCount()
}
