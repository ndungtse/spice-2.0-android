package org.medtroniclabs.uhis.ncd.assessment.repo

import org.medtroniclabs.uhis.data.APIResponse
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.ncd.data.BPBGListModel
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import javax.inject.Inject

class BloodPressureRepo @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper,
) {
    fun riskFactorListing() = roomHelper.getRiskFactorEntity()

    suspend fun createBpLog(hashMap: HashMap<String, Any>): Resource<APIResponse<HashMap<String, Any>>> =
        try {
            val response = apiHelper.bpLogCreate(hashMap)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun bpLogList(request: BPBGListModel): Resource<BPBGListModel> =
        try {
            val response = apiHelper.bpLogList(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun createBpLogForNurse(hashMap: HashMap<String, Any>): Resource<APIResponse<HashMap<String, Any>>> =
        try {
            val response = apiHelper.bpLogCreateForNurse(hashMap)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
}
