package com.medtroniclabs.spice.ui.mypatients.repo

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.db.response.VillageBasicDetails
import com.medtroniclabs.spice.network.ApiHelper
import javax.inject.Inject

class PatientRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    suspend fun getVillageIdName() : List<VillageBasicDetails> {
        return roomHelper.getVillageIdName()
    }
}