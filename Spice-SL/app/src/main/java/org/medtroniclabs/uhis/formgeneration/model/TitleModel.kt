package org.medtroniclabs.uhis.formgeneration.model

data class TitleModel(
    val role: String,
    val title: String,
    val titleCulture: String? = null,
)
