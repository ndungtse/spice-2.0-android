package com.medtroniclabs.spice.model

data class PatientsDataModel(
    var skip: Int? = null,
    var limit: Int? = null,
    val villageIds: List<Long>? = null,
    val searchText:String? = null,
    val districtId:Long? = null
)
