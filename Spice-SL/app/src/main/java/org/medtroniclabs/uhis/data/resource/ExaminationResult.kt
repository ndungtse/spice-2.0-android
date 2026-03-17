package org.medtroniclabs.uhis.data.resource

data class ExaminationResult(
    val index: Int? = null,
    val symptomsTitle: String? = null,
    val description: List<String>? = null,
)
