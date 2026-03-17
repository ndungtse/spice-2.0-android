package org.medtroniclabs.uhis.ui.dashboard.ncd.repository

import org.medtroniclabs.uhis.common.StringConverter.getErrorMessage
import org.medtroniclabs.uhis.data.NCDUserDashboardRequest
import org.medtroniclabs.uhis.data.NCDUserDashboardResponse
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import javax.inject.Inject

class NCDDashBoardRepository @Inject constructor(
    private var apiHelper: ApiHelper,
) {
    suspend fun getUserDashboardDetails(request: NCDUserDashboardRequest): Resource<NCDUserDashboardResponse> =
        try {
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
