package com.medtroniclabs.spice.data.model

data class TreatmentItem(
    val name: String,
    var startDate: String? = null,
    var endDate: String? = null,
    var value: String,
)
