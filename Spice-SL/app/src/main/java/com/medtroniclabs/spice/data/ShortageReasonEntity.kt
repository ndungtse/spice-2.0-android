package com.medtroniclabs.spice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shortageReason")
data class ShortageReasonEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val type: String,
    val displayOrder: Int,
    val displayValue: String,
)

