package com.medtroniclabs.spice.ui.medicalreview.pharmacist.repo

import com.medtroniclabs.spice.common.StringConverter.getErrorMessage
import com.medtroniclabs.spice.data.DispensePrescriptionRequest
import com.medtroniclabs.spice.data.DispensePrescriptionResponse
import com.medtroniclabs.spice.data.DispenseUpdateRequest
import com.medtroniclabs.spice.data.DispenseUpdateResponse
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class NCDPharmacistRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper,
) {
    suspend fun getPrescriptionDispenseList(request: DispenseUpdateRequest): Resource<ArrayList<DispensePrescriptionResponse>> =
        try {
            val response = apiHelper.getPrescriptionDispenseList(request)
            if (response.isSuccessful) {
                response.body()?.entityList?.let {
                    Resource(state = ResourceState.SUCCESS, it)
                } ?: Resource(state = ResourceState.ERROR)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getDispensePrescriptionHistory(request: DispenseUpdateRequest): Resource<ArrayList<DispensePrescriptionResponse>> =
        try {
            val response = apiHelper.getDispensePrescriptionHistory(request)
            if (response.isSuccessful) {
                response.body()?.entityList?.let {
                    Resource(state = ResourceState.SUCCESS, it)
                } ?: Resource(state = ResourceState.ERROR)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getShortageReasonList(type: String) = roomHelper.getNCDShortageReason(type)

    suspend fun updateDispensePrescription(request: DispensePrescriptionRequest): Resource<DispenseUpdateResponse> =
        try {
            val response = apiHelper.updateDispensePrescription(request)
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
