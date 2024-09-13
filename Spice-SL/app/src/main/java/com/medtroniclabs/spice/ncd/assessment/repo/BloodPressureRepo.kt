package com.medtroniclabs.spice.ncd.assessment.repo

import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.ncd.data.BPBGListModel
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class BloodPressureRepo @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper
) {
    fun riskFactorListing() = roomHelper.getRiskFactorEntity()

    suspend fun createBpLog(hashMap: HashMap<String, Any>): Resource<APIResponse<HashMap<String, Any>>> {
        return try {
            val response = apiHelper.bpLogCreate(hashMap)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun bpLogList(request: BPBGListModel): Resource<BPBGListModel> {
        return try {
            val response = apiHelper.bpLogList(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }
}