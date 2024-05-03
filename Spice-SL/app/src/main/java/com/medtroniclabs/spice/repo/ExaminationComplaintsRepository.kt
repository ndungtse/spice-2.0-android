package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.resource.Resource
import javax.inject.Inject

class ExaminationComplaintsRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {
    suspend fun getComplaintsListByType(
        type: String,
        presentingComplaintList: MutableLiveData<Resource<List<MedicalReviewMetaItems>>>
    ) {
        try {
            presentingComplaintList.postLoading()
            val response = roomHelper.getExaminationsComplaintByType(type)
            presentingComplaintList.postSuccess(response)
        } catch (e: Exception) {
            presentingComplaintList.postError()
        }
    }
}