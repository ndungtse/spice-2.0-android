package com.medtroniclabs.spice.ncd.counseling.repo

import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ncd.counseling.model.NCDCounselingModel
import javax.inject.Inject

class CounselingRepo @Inject constructor(
    private var apiHelper: ApiHelper, private var roomHelper: RoomHelper
) {
    fun getLifestyleAssessments(type: String? = null, category: String) =
        roomHelper.getComorbidities(type, category)

    suspend fun createAssessment(request: NCDCounselingModel): Resource<APIResponse<NCDCounselingModel>> {
        return try {
            val response = apiHelper.createLifestyle(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun updateAssessment(request: NCDCounselingModel): Resource<APIResponse<NCDCounselingModel>> {
        return try {
            val response = apiHelper.updateLifestyle(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getAssessmentList(request: NCDCounselingModel): Resource<APIResponse<ArrayList<NCDCounselingModel>>> {
        return try {
            val response = apiHelper.getLifestyleList(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun removeAssessment(request: NCDCounselingModel): Resource<APIResponse<NCDCounselingModel>> {
        return try {
            val response = apiHelper.removeLifestyle(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(
                    state = ResourceState.SUCCESS,
                    data = response.body()?.let {
                        APIResponse(
                            status = it.status,
                            entity = request,
                            responseCode = it.responseCode
                        )
                    })
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }
}