package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.LabTestCreateRequest
import com.medtroniclabs.spice.model.LabTestListRequest
import com.medtroniclabs.spice.model.LabTestListResponse
import com.medtroniclabs.spice.model.RemoveLabTestRequest
import com.medtroniclabs.spice.model.medicalreview.SearchLabTestResponse
import com.medtroniclabs.spice.model.medicalreview.SearchRequestLabTest
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class InvestigationRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    suspend fun searchInvestigationByName(request: SearchRequestLabTest): Resource<ArrayList<SearchLabTestResponse>> {
        return try {
            val response = apiHelper.searchLabTestByName(request)
            if (response.isSuccessful) {
                response.body()?.entityList?.let {
                    Resource(ResourceState.SUCCESS, response.body()?.entityList)
                } ?: kotlin.run {
                    Resource(ResourceState.ERROR)
                }
            } else {
                Resource(ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }
    }


    suspend fun createLabTest(request: LabTestCreateRequest): Resource<Map<String, Any>> {
        return try {
            val response = apiHelper.createLabTest(request)
            if (response.isSuccessful) {
                response.body()?.entity?.let {
                    Resource(ResourceState.SUCCESS, response.body()?.entity)
                } ?: kotlin.run {
                    Resource(ResourceState.ERROR)
                }
            } else {
                Resource(ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }
    }

    suspend fun getLabTestList(request: LabTestListRequest): Resource<ArrayList<LabTestListResponse>> {
        return try {
            val response = apiHelper.getLabTestList(request)
            if (response.isSuccessful) {
                response.body()?.entityList?.let {
                    Resource(ResourceState.SUCCESS, response.body()?.entityList)
                } ?: kotlin.run {
                    Resource(ResourceState.ERROR)
                }
            } else {
                Resource(ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }
    }

    suspend fun removeLabTest(request: RemoveLabTestRequest): Resource<Map<String, Any>> {
        return try {
            val response = apiHelper.removeLabTest(request)
            if (response.isSuccessful) {
                response.body()?.entity?.let {
                    Resource(ResourceState.SUCCESS, response.body()?.entity)
                } ?: kotlin.run {
                    Resource(ResourceState.ERROR)
                }
            } else {
                Resource(ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }
    }

}