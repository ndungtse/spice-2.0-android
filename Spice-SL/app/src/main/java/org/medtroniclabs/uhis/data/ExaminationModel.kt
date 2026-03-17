package org.medtroniclabs.uhis.data

import org.medtroniclabs.uhis.formgeneration.model.FormLayout

data class ExaminationModel(
    val diseaseName: String,
    val questionnaires: ArrayList<FormLayout>,
)
