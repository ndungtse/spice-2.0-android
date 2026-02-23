package com.medtroniclabs.spice.ui.assessment

data class SymptomResponse(
    val otherSymptom: String? = null,
    val name: String,
    val type: String? = null,
    val cultureValue: String? = null,
)
