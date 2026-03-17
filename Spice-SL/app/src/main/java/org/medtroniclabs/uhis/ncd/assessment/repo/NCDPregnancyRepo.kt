package org.medtroniclabs.uhis.ncd.assessment.repo

import org.medtroniclabs.uhis.data.APIResponse
import org.medtroniclabs.uhis.data.PregnancyDetailsModel
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import javax.inject.Inject

class NCDPregnancyRepo @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper,
) {
    suspend fun ncdPregnancyCreate(requestModel: PregnancyDetailsModel): Resource<APIResponse<HashMap<String, Any>>> =
        try {
            val response = apiHelper.ncdPregnancyCreate(requestModel)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun ncdPregnancyDetails(requestModel: HashMap<String, Any>): Resource<PregnancyDetailsModel> =
        try {
            val response = apiHelper.ncdPregnancyDetails(requestModel)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }
}
