package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.db.local.RoomHelper
import javax.inject.Inject

class ExaminationsRepository @Inject constructor(
    val roomHelper: RoomHelper,
) {
    suspend fun getExaminationQuestionsByWorkFlow(workFlowType: String) = roomHelper.getExaminationQuestionsByWorkFlow(workFlowType)
}
