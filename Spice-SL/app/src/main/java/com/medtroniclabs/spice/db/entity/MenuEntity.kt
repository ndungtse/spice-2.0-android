package com.medtroniclabs.spice.db.entity

import androidx.room.PrimaryKey

data class MenuEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var name: String,
    var role: String,
    var menuId:String,
    var roleDisplayName: String,
    val displayOrder: Int = 0,
)
