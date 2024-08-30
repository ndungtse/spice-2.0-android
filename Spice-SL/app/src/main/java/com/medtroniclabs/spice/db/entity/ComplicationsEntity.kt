package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ComplicationsEntity")
data class ComplicationsEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val displayValue: String,
    val displayOrder: Int
)
