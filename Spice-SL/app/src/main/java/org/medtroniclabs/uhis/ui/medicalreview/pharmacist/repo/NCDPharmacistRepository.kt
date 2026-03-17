package org.medtroniclabs.uhis.ui.medicalreview.pharmacist.repo

import org.medtroniclabs.uhis.common.StringConverter.getErrorMessage
import org.medtroniclabs.uhis.data.DispensePrescriptionRequest
import org.medtroniclabs.uhis.data.DispensePrescriptionResponse
import org.medtroniclabs.uhis.data.DispenseUpdateRequest
import org.medtroniclabs.uhis.data.DispenseUpdateResponse
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
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
