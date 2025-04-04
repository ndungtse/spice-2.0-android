package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.db.entity.TreatmentDetailsEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class TreatmentDetailsRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {

    suspend fun getTreatmentDetails(memberId: String): TreatmentDetailsEntity? {
        return roomHelper.getTreatmentDetails(memberId)
    }
}