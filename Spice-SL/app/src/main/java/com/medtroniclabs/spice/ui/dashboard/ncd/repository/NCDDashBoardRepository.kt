package com.medtroniclabs.spice.ui.dashboard.ncd.repository

import com.medtroniclabs.spice.common.StringConverter.getErrorMessage
import com.medtroniclabs.spice.data.NCDUserDashboardRequest
import com.medtroniclabs.spice.data.NCDUserDashboardResponse
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class NCDDashBoardRepository @Inject constructor(
    private var apiHelper: ApiHelper,
) {

    suspend fun getUserDashboardDetails(request: NCDUserDashboardRequest): Resource<NCDUserDashboardResponse> {
        return try {
            val response = apiHelper.getUserDashboardDetails(request)
            if (response.isSuccessful) {
                response.body()?.entity?.let {
                    Resource(state = ResourceState.SUCCESS, it)
                } ?: Resource(state = ResourceState.ERROR)
            } else {
                Resource(state = ResourceState.ERROR, message = getErrorMessage(response.errorBody()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    }
}
