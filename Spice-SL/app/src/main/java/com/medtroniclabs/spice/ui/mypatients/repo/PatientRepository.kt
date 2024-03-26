package com.medtroniclabs.spice.ui.mypatients.repo

import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.db.response.VillageBasicDetails
import com.medtroniclabs.spice.network.ApiHelper
import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.model.PatientDetailReq
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.Resource
import javax.inject.Inject

class PatientRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    suspend fun getVillageIdName() : List<VillageBasicDetails> {
        return roomHelper.getVillageIdName()
    }

    suspend fun getPatients(
        patientsLiveData: MutableLiveData<Resource<PatientListRespModel>>,
        request: PatientDetailReq
    ) {
        try {
            patientsLiveData.postLoading()
            val response = apiHelper.getPatient(request)
            if (response.isSuccessful) {
                response.body()?.entity?.let {
                    patientsLiveData.postSuccess(it)
                }
            } else {
                patientsLiveData.postError()
            }
        } catch (e: Exception) {
            patientsLiveData.postError()
        }
    }
}