package org.medtroniclabs.uhis.formgeneration

import org.medtroniclabs.uhis.formgeneration.model.FormLayout

interface ExaminationListener {
    fun onDialogueCheckboxListener(
        id: String,
        formLayout: FormLayout,
        resultMap: Any?,
        diseaseName: String,
    )

    fun setResultHashMap(resultMap: HashMap<String, Any>)
}
