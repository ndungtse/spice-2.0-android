package org.medtroniclabs.uhis.ncd.medicalreview.repo

import org.medtroniclabs.uhis.data.APIResponse
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.ncd.data.NCDTreatmentPlanModel
import org.medtroniclabs.uhis.ncd.data.NCDTreatmentPlanModelDetails
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
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
