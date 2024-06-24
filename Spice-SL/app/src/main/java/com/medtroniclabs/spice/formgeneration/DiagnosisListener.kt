package com.medtroniclabs.spice.formgeneration

import com.medtroniclabs.spice.formgeneration.model.FormLayout

interface DiagnosisListener {

    fun onDiagnosisSelection(isEmptyOrNot: Boolean)
}