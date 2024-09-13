package com.medtroniclabs.spice.ncd.assessment.repo

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import javax.inject.Inject

class AssessmentRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper
) {
    fun getFormData(
        formType: String,
    ): LiveData<String> {
        return roomHelper.getFormDataForNcd(formType)
    }
}