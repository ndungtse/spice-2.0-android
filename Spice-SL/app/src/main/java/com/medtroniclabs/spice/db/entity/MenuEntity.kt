package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MenuEntity")
data class MenuEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    var menuId:String,
    val roleName: String? = null,
    val name: String,
    val displayOrder: Int,
)
