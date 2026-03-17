package org.medtroniclabs.uhis.ncd.counseling.repo

import org.medtroniclabs.uhis.data.APIResponse
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.ncd.data.AssessmentResultModel
import org.medtroniclabs.uhis.ncd.data.NCDCounselingModel
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import javax.inject.Inject

class CounselingRepo @Inject constructor(
    private var apiHelper: ApiHelper, private var roomHelper: RoomHelper,
) {
    fun getLifestyleAssessments(
        type: String? = null,
        category: String,
    ) = roomHelper.getComorbidities(type, category)

    suspend fun createAssessment(
        request: NCDCounselingModel,
        lifestyle: Boolean,
    ): Resource<APIResponse<NCDCounselingModel>> =
        try {
            val response = if (lifestyle) apiHelper.createLifestyle(request) else apiHelper.createPsychological(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun updateAssessment(
        request: AssessmentResultModel,
        lifestyle: Boolean,
    ): Resource<APIResponse<HashMap<String, Any>>> =
        try {
            val response = if (lifestyle) apiHelper.updateLifestyle(request) else apiHelper.updatePsychological(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getAssessmentList(
        request: NCDCounselingModel,
        lifestyle: Boolean,
    ): Resource<APIResponse<ArrayList<NCDCounselingModel>>> =
        try {
            val response = if (lifestyle) apiHelper.getLifestyleList(request) else apiHelper.getPsychological(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun removeAssessment(
        request: NCDCounselingModel,
        lifestyle: Boolean,
    ): Resource<APIResponse<NCDCounselingModel>> =
        try {
            val response = if (lifestyle) apiHelper.removeLifestyle(request) else apiHelper.removePsychological(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
}
