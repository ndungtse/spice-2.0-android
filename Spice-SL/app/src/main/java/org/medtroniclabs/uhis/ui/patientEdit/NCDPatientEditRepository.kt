package org.medtroniclabs.uhis.ui.patientEdit

import org.medtroniclabs.uhis.common.StringConverter
import org.medtroniclabs.uhis.ncd.data.NCDPatientTransferValidate
import org.medtroniclabs.uhis.ncd.data.NCDRegionSiteModel
import org.medtroniclabs.uhis.ncd.data.NCDSiteRoleModel
import org.medtroniclabs.uhis.ncd.data.NCDSiteRoleResponse
import org.medtroniclabs.uhis.ncd.data.NCDTransferCreateRequest
import org.medtroniclabs.uhis.ncd.data.RegionSiteResponse
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import javax.inject.Inject

class NCDPatientEditRepository @Inject constructor(
    private var apiHelper: ApiHelper,
) {
    suspend fun ncdUpdatePatientDetail(request: HashMap<String, Any>): Resource<HashMap<String, Any>> =
        try {
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

    suspend fun validatePatientTransfer(request: NCDPatientTransferValidate): Resource<HashMap<String, Any>> =
        try {
            val response = apiHelper.validatePatientTransfer(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body())
            } else {
                Resource(
                    state = ResourceState.ERROR,
                    message = StringConverter.getErrorMessage(response.errorBody()),
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun createPatientTransfer(request: NCDTransferCreateRequest): Resource<String>? =
        try {
            val response = apiHelper.createPatientTransfer(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.message)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun searchSite(request: NCDRegionSiteModel): Resource<ArrayList<RegionSiteResponse>> =
        try {
            val response = apiHelper.searchSite(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entityList)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun searchRoleUser(request: NCDSiteRoleModel): Resource<ArrayList<NCDSiteRoleResponse>> =
        try {
            val response = apiHelper.searchRoleUser(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, response.body()?.entityList)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }
}
