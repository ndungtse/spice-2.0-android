package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.formgeneration.model.FormLayout

data class ExaminationModel(
    val diseaseName: String,
    val questionnaires: ArrayList<FormLayout>,
)
