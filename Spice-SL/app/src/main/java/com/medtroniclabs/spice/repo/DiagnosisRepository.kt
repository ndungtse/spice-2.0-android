package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import javax.inject.Inject

class DiagnosisRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {

    suspend fun getDiagnosisList(
        diagnosisList: MutableLiveData<Resource<List<DiseaseCategoryItems>>>
    ){
        try {
            diagnosisList.postLoading()
            val response = roomHelper.getDiagnosisList()
            diagnosisList.postSuccess(response)
        } catch (e: Exception) {
            diagnosisList.postError()
        }
    }

}