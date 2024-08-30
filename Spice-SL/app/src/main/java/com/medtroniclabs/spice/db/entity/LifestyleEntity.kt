package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LifestyleEntity")
data class LifestyleEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val displayValue: String? = null,
    val answers: ArrayList<LifeStyleAnswer>,
    val type: String,
    val displayOrder: Int,
)

data class LifeStyleAnswer(
    val name: String,
    val isAnswerDependent: Boolean
)
