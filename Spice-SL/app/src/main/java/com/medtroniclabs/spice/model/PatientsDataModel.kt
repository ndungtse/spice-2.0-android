package com.medtroniclabs.spice.model

data class PatientsDataModel(
    var skip: Int? = null,
    var limit: Int? = null,
    val villageNames: List<String>? = null,
    val searchText:String? = null
)
