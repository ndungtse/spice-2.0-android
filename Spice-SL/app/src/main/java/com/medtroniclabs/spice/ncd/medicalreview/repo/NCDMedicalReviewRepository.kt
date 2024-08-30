package com.medtroniclabs.spice.ncd.medicalreview.repo

import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import javax.inject.Inject

class NCDMedicalReviewRepository@Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    fun getSymptomListByTypeForNCD (type:String) = roomHelper.getSymptomListByTypeForNCD(type)
}