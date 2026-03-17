package org.medtroniclabs.uhis.data.model

data class MultiSelectDropDownModel(
    var id: Long,
    var name: String,
    var displayValue: String? = null,
    var value: String?,
)
