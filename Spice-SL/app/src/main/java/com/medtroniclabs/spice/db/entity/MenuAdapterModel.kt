package com.medtroniclabs.spice.db.entity

data class MenuAdapterModel(
    var id: Int? = null,
    var name: String,
    var role: String,
    var menuId:String,
    val displayOrder: Long = 0,
)
