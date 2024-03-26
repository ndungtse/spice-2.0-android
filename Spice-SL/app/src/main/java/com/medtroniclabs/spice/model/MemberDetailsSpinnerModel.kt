package com.medtroniclabs.spice.model

data class MemberDetailsSpinnerModel(
    var id: Long,
    var name: String,
    var age: String? = null,
    var gender: String? = null,
    var dob: String? = null
)