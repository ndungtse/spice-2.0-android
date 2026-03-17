package org.medtroniclabs.uhis.ncd.assessment.repo

import org.medtroniclabs.uhis.data.APIResponse
import org.medtroniclabs.uhis.ncd.data.BPBGListModel
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import javax.inject.Inject

class GlucoseRepo @Inject constructor(private val apiHelper: ApiHelper) {
    suspend fun glucoseLogCreate(hashMap: HashMap<String, Any>): Resource<APIResponse<HashMap<String, Any>>> =
        try {
            val response = apiHelper.glucoseLogCreate(hashMap)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun glucoseLogList(request: BPBGListModel): Resource<BPBGListModel> =
        try {
            val response = apiHelper.glucoseLogList(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun glucoseLogCreateForNurse(hashMap: HashMap<String, Any>): Resource<APIResponse<HashMap<String, Any>>> =
        try {
            val response = apiHelper.glucoseLogCreateForNurse(hashMap)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
}
