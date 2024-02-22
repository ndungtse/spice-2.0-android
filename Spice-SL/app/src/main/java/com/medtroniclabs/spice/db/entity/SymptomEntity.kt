package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SymptomEntity")
data class SymptomEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val type: String,
    val displayOrder: Int?
)
