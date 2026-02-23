package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DosageDurationEntity")
data class DosageDurationEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val displayOrder: Int,
    val displayValue: String? = null,
    val quantity: Long,
)
