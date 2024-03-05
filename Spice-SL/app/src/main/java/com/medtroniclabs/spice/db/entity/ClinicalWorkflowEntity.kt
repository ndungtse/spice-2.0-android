package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ClinicalWorkflowEntity")
data class ClinicalWorkflowEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val moduleType: String?=null,
    val workflowName: String,
    val countryId: Long,
    val displayOrder:Int? = null,
)
