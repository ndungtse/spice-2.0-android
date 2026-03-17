package org.medtroniclabs.uhis.repo

import com.google.gson.Gson
import org.medtroniclabs.uhis.data.ErrorResponse
import org.medtroniclabs.uhis.data.performance.ChwVillageFilterModel
import org.medtroniclabs.uhis.data.performance.FilterPreference
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import okhttp3.ResponseBody
import javax.inject.Inject

class PerformanceMonitoringRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper,
) {
    suspend fun getLinkedChwDetails(): Resource<List<ChwVillageFilterModel>> =
        try {
            val response = apiHelper.getPeerSupervisorLinkedChwList()
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entityList)
            } else {
                Resource(
                    state = ResourceState.ERROR,
                    message = getErrorMessage(response.errorBody()),
                )
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getUserFilterPreference(request: FilterPreference): Resource<FilterPreference> =
        try {
            val response = apiHelper.getUserFilterPreference(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(
                    state = ResourceState.ERROR,
                    message = getErrorMessage(response.errorBody()),
                )
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun saveUserFilterPreference(request: FilterPreference): Resource<FilterPreference> =
        try {
            val response = apiHelper.saveUserFilterPreference(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(
                    state = ResourceState.ERROR,
                    message = getErrorMessage(response.errorBody()),
                )
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    private fun getErrorMessage(errorBody: ResponseBody?): String? {
        if (errorBody == null) {
            return null
        }
        return try {
            val errorResponse = Gson().fromJson(errorBody.string(), ErrorResponse::class.java)
            return errorResponse.message
        } catch (e: Exception) {
            null
        }
    }
}
