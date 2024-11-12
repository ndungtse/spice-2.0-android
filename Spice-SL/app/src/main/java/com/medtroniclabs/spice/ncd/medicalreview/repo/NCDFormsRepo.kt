package com.medtroniclabs.spice.ncd.medicalreview.repo

import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.db.local.RoomHelper
import javax.inject.Inject

class NCDFormsRepo @Inject constructor(
    private var roomHelper: RoomHelper
) {
    suspend fun getNCDForm(
        formType: String,
        customizedFormType: String = DefinedParams.Workflow,
        workFlow: String? = null
    ): List<String> {
        return if (workFlow.isNullOrBlank()) roomHelper.getNCDForm(
            formType,
            customizedFormType
        ) else{
            val types = listOf(formType, workFlow)
            roomHelper.getAssessmentFormData(types, workFlow)
        }
    }
}