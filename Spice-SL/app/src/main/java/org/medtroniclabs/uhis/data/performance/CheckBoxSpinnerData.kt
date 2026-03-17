package org.medtroniclabs.uhis.data.performance

data class CheckBoxSpinnerData(
    val id: Long,
    val name: String,
    var isSelected: Boolean,
    var chwId: Long? = null,
)
