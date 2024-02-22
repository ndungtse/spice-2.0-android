package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MenuEntity")
data class MenuEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val roleName: String,
    val menus: String,
    val active: Boolean?,
    val deleted: Boolean?,
    val menuType: String
)

data class Menu(
    val id: Long,
    val order: Long,
    val name: String
)
