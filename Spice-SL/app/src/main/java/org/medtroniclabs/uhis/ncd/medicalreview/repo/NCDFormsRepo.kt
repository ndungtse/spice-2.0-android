package org.medtroniclabs.uhis.ncd.medicalreview.repo

import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.db.local.RoomHelper
import javax.inject.Inject

class NCDFormsRepo @Inject constructor(
    private var roomHelper: RoomHelper,
) {
    suspend fun getNCDForm(
        formType: String,
        customizedFormType: String = DefinedParams.Workflow,
        workFlow: String? = null,
    ): List<String> =
        if (workFlow.isNullOrBlank()) {
            roomHelper.getNCDForm(
                formType,
                customizedFormType,
            )
        } else {
            val types = listOf(formType, workFlow)
            roomHelper.getAssessmentFormData(types, workFlow)
        }
}
