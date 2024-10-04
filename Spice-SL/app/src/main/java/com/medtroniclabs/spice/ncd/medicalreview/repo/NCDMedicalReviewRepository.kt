package com.medtroniclabs.spice.ncd.medicalreview.repo

import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import javax.inject.Inject

class NCDMedicalReviewRepository@Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    fun getSymptomListByTypeForNCD (type:String) = roomHelper.getSymptomListByTypeForNCD(type)

    fun getComorbiditiesBasedOnType(type: String? = null, category: String) =
        roomHelper.getComorbidities(type, category)

    fun getLifeStyle() = roomHelper.getLifeStyle()
    fun getNCDDiagnosisList(types: List<String>) = roomHelper.getNCDDiagnosisList(types)
}