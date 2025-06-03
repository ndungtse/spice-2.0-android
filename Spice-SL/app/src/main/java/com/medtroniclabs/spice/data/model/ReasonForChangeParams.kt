package com.medtroniclabs.spice.data.model


data class ReasonForChangeParams(
    val medicationNames: String,
    val regimenLine: Int?,
    val discontinuedReason: String,
    val medicationList: List<String>? = null
)

