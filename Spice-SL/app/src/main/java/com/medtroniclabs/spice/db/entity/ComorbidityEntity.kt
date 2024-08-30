package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ComorbidityEntity")
data class ComorbidityEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val displayValue: String,
    val displayOrder: Int,
    val type: String? = null
)