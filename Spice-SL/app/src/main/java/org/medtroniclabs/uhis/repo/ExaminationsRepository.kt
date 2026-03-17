package org.medtroniclabs.uhis.repo

import org.medtroniclabs.uhis.db.local.RoomHelper
import javax.inject.Inject

class ExaminationsRepository @Inject constructor(
    val roomHelper: RoomHelper,
) {
    suspend fun getExaminationQuestionsByWorkFlow(workFlowType: String) = roomHelper.getExaminationQuestionsByWorkFlow(workFlowType)
}
