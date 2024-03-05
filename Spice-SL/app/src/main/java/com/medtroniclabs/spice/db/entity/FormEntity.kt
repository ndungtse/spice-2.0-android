package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FormEntity")
data class FormEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val formInput: String,
    val formType: String,
)