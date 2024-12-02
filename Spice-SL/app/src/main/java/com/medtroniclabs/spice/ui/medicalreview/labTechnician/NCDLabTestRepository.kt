package com.medtroniclabs.spice.ui.medicalreview.labTechnician

import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.LabTestCreateRequest
import com.medtroniclabs.spice.model.LabTestListRequest
import com.medtroniclabs.spice.model.LabTestListResponse
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class NCDLabTestRepository @Inject constructor(
    private val apiHelper: ApiHelper
) {

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

    suspend fun updateLabTest(request: LabTestCreateRequest): Resource<APIResponse<Map<String, Any>>> {
        return try {
            val response = apiHelper.updateLabTest(request)
            if (response.isSuccessful)
                Resource(ResourceState.SUCCESS, response.body())
            else
                Resource(ResourceState.ERROR)
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }
    }

}