package com.medtroniclabs.spice.ui.mypatients.repo

import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.data.PrescriptionListRequest
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.db.response.VillageBasicDetails
import com.medtroniclabs.spice.model.PatientDetailRequest
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class PatientRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    suspend fun getVillageIdName(): List<VillageBasicDetails> {
        return roomHelper.getVillageIdName()
    }

    suspend fun getPatients(
        request: PatientDetailRequest
    ): Resource<PatientListRespModel> {
        return try {
            val response =
                if (CommonUtils.isNonNcdWorkflow()) apiHelper.getPatient(request) else apiHelper.getNcdPatient(
                    request
                )
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }
}