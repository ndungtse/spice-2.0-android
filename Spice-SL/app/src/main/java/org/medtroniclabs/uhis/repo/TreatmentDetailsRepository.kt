package org.medtroniclabs.uhis.repo

import org.medtroniclabs.uhis.db.entity.TreatmentDetailsEntity
import org.medtroniclabs.uhis.db.local.RoomHelper
import javax.inject.Inject

class TreatmentDetailsRepository @Inject constructor(
    private var roomHelper: RoomHelper,
) {
    suspend fun getTreatmentDetails(memberId: String): TreatmentDetailsEntity? = roomHelper.getTreatmentDetails(memberId)
}
