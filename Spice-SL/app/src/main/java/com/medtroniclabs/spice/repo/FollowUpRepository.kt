package com.medtroniclabs.spice.repo

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.followup.FollowUpFilter
import com.medtroniclabs.spice.network.ApiHelper
import javax.inject.Inject

class FollowUpRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper
) {

    fun getFollowUpListLiveData(filter: FollowUpFilter): LiveData<List<FollowUpPatientModel>> {
        return roomHelper.getFollowUpPatientListLiveData(filter)
    }
}