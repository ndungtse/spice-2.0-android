package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import javax.inject.Inject

class AssessmentRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {
    suspend fun saveAssessment(assessmentEntity: AssessmentEntity): Long =
        roomHelper.saveAssessment(assessmentEntity)
}