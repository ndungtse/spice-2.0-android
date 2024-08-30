package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PhysicalExaminationEntity")
data class PhysicalExaminationEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val displayValue: String,
    val displayOrder: Int,
    val type: String? = null
)
