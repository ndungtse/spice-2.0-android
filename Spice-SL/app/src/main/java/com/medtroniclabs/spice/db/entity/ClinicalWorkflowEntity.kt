package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ClinicalWorkflowEntity")
data class ClinicalWorkflowEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val moduleType: String,
    val workflowName: String,
    val countryId: Long,
    val active: Boolean,
    val deleted: Boolean,
    val order:Int
)
