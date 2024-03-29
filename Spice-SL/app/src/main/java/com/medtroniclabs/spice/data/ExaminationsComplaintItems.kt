package com.medtroniclabs.spice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ExaminationComplaintsEntity")
data class ExaminationsComplaintItems(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long,
    val id: Long,
    val name: String,
    val ageCondition: String? = null,
    var type: String? = null,
    val displayOrder: Int
)
