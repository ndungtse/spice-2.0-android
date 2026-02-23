package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.db.entity.TreatmentDetailsEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import javax.inject.Inject

class TreatmentDetailsRepository @Inject constructor(
    private var roomHelper: RoomHelper,
) {
    suspend fun getTreatmentDetails(memberId: String): TreatmentDetailsEntity? = roomHelper.getTreatmentDetails(memberId)
}
