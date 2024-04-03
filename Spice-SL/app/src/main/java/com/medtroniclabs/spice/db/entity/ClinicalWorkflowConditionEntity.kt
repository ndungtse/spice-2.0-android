package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ClinicalWorkflowConditionEntity")
data class ClinicalWorkflowConditionEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val gender: String? = null,
    val maxAge: Int? = null,
    val minAge: Int? = null,
    val clinicalWorkflowId: Long,
    val subModule: String? = null
)
