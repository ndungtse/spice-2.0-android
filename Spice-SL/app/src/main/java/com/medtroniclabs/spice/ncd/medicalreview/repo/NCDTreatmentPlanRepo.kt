package com.medtroniclabs.spice.ncd.medicalreview.repo

import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.ncd.data.NCDTreatmentPlanModel
import com.medtroniclabs.spice.ncd.data.NCDTreatmentPlanModelDetails
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class NCDTreatmentPlanRepo @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper,
) {
    fun getFrequencies() = roomHelper.getFrequencies()

    suspend fun updateNCDTreatmentPlan(request: NCDTreatmentPlanModel): Resource<APIResponse<NCDTreatmentPlanModel>> =
        try {
            val response = apiHelper.updateNCDTreatmentPlan(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getNCDTreatmentPlan(request: NCDTreatmentPlanModelDetails): Resource<APIResponse<NCDTreatmentPlanModelDetails>> =
        try {
            val response = apiHelper.getNCDTreatmentPlan(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
}
