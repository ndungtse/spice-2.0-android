package com.medtroniclabs.spice.formgeneration

import com.medtroniclabs.spice.formgeneration.model.FormLayout

interface DiagnosisListener {
    fun onDialogueItemCheckListener(
        id: String,
        formLayout: FormLayout,
        resultMap: Any?,
        diseaseName: String
    )
}