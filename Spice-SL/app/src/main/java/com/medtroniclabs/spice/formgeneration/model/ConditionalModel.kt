package com.medtroniclabs.spice.formgeneration.model

data class ConditionalModel(
    var targetId: String?,
    var visibility: String?,
    var eq: String?,
    var eqList: ArrayList<String>? = null,
    var lengthGreaterThan: Int? = null,
    var greaterThanOrEqual: Double? = null,
    var lessThanOrEqual: Double? = null,
    var enabled: Boolean? = null,
    var targetOption: String? = null,
)
