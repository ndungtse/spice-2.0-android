package org.medtroniclabs.uhis.ui.medicalreview.labTechnician

import org.medtroniclabs.uhis.data.APIResponse
import org.medtroniclabs.uhis.model.LabTestCreateRequest
import org.medtroniclabs.uhis.model.LabTestListRequest
import org.medtroniclabs.uhis.model.LabTestListResponse
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import javax.inject.Inject

class NCDLabTestRepository @Inject constructor(
    private val apiHelper: ApiHelper,
) {
    suspend fun getLabTestList(request: LabTestListRequest): Resource<ArrayList<LabTestListResponse>> =
        try {
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

    suspend fun updateLabTest(request: LabTestCreateRequest): Resource<APIResponse<Map<String, Any>>> =
        try {
            val response = apiHelper.updateLabTest(request)
            if (response.isSuccessful) {
                Resource(ResourceState.SUCCESS, response.body())
            } else {
                Resource(ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }
}
