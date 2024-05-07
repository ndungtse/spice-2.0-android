package com.medtroniclabs.spice.formgeneration

import com.medtroniclabs.spice.formgeneration.model.FormLayout

interface ExaminationListener {
    fun onDialogueCheckboxListener(
        id: String,
        formLayout: FormLayout,
        resultMap: Any?,
        diseaseName: String
    )

    fun setResultHashMap(resultMap: HashMap<String, Any>)
}