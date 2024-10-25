package com.medtroniclabs.spice.ui.patientEdit

import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class NCDPatientEditRepository @Inject constructor(
    private var apiHelper: ApiHelper
) {
    suspend fun ncdUpdatePatientDetail(request: HashMap<String, Any>): Resource<HashMap<String, Any>> {
        return try {
            val response = apiHelper.ncdUpdatePatientDetail(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }
    }
}