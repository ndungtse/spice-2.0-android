package com.medtroniclabs.spice.repo

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.community.CommunityPopulationStatistics
import com.medtroniclabs.spice.data.community.CommunityProfile
import com.medtroniclabs.spice.db.entity.CommunityDetailsEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class CommunityProfileRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper
) {

    fun getFilterVillageWithHouseholds(searchText: String): LiveData<List<CommunityProfile>> {
        return roomHelper.getFilterVillagesWithHouseholdsCount(searchText)
    }

    suspend fun getCommunityStatistics(villageId: Long): Resource<CommunityPopulationStatistics> {
        return try {
            val response = roomHelper.getCommunityStatistics(villageId)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR, data = null)
        }
    }

    suspend fun getFormData(
        formType: String
    ): Resource<FormResponse> {
        return try {
            val response = roomHelper.getFormData(formType)
            val formFieldsType = object : TypeToken<FormResponse>() {}.type
            val formFields: FormResponse = Gson().fromJson(response, formFieldsType)
            Resource(state = ResourceState.SUCCESS, data = formFields)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getNearestHealthFacility(): Resource<ArrayList<Map<String,Any>>> {
        val healthFacilityList = roomHelper.getNearestHealthFacility()
        val dropDownList = ArrayList<Map<String, Any>>()
        for ((_, healthFacilityEntity) in healthFacilityList.withIndex()) {
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to healthFacilityEntity.name,
                    DefinedParams.id to healthFacilityEntity.fhirId.toString()
                )
            )
        }
        return Resource(
            state = ResourceState.SUCCESS,
            data = dropDownList
        )
    }


    suspend fun createCommunityProfile(request: HashMap<String, Any>): Resource<APIResponse<HashMap<String, Any>>> {
        return try {
            val response = apiHelper.createCommunityProfile(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR, data = null)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR, data = null)
        }
    }

    suspend fun updateCommunityProfile(request: HashMap<String, Any>): Resource<APIResponse<HashMap<String, Any>>> {
        return try {
            val response = apiHelper.updateCommunityProfile(request)
            if(response.isSuccessful){
                Resource(state = ResourceState.SUCCESS, data = response.body())
            }else{
                Resource(state = ResourceState.ERROR, data = null)
            }
        }catch (e:Exception){
            Resource(state = ResourceState.ERROR, data = null)
        }
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

    suspend fun getCommunityProfileDetails(villageId: Long): Resource<CommunityDetailsEntity>{
        return try {
            val response = roomHelper.getCommunityDetails(villageId)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR, data = null)
        }
    }

    suspend fun updateCommunityProfileDetails(
        villageId: Long,
        description: String,
        regDate: String,
        payload: String
    ): Resource<Unit> {
        return try {
            val latitude = SecuredPreference.getDouble(
                SecuredPreference.EnvironmentKey.CURRENT_LATITUDE.name,
                0.0
            )
            val longitude = SecuredPreference.getDouble(
                SecuredPreference.EnvironmentKey.CURRENT_LONGITUDE.name,
            )
            val updateCommunityDetailsEntity = CommunityDetailsEntity(
                villageId = villageId,
                communityDescription = description,
                registeredDate = regDate,
                payload = payload,
                latitude = latitude,
                longitude = longitude
            )
            val response = roomHelper.updateCommunityDetails(updateCommunityDetailsEntity)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR, data = null)
        }
    }

    suspend fun insertCommunityProfileDetails(villageId:Long,
                                              description:String,
                                              regDate:String,
                                              payload:String):Resource<Unit>{
        return try {
            val latitude = SecuredPreference.getDouble(
                SecuredPreference.EnvironmentKey.CURRENT_LATITUDE.name,
                0.0
            )
            val longitude = SecuredPreference.getDouble(
                SecuredPreference.EnvironmentKey.CURRENT_LONGITUDE.name,
                0.0
            )
            val communityDetailsEntity = CommunityDetailsEntity(
                villageId = villageId,
                communityDescription = description,
                registeredDate = regDate,
                payload = payload,
                latitude = latitude,
                longitude = longitude
            )
            val response = roomHelper.insertCommunityDetails(communityDetailsEntity)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e:Exception) {
            Resource(state = ResourceState.ERROR, data = null)
        }
    }

    suspend fun isCommunityProfileExists(villageId: Long): Int {
        return roomHelper.isCommunityExist(villageId)
    }

    suspend fun updateUnSynStatus(villageId: Long,unSynStatus:String){
        return roomHelper.updateUnSynStatus(villageId,unSynStatus)
    }
}