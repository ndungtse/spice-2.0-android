package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HealthFacilityEntity")
data class HealthFacilityEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val type: String?,
    val latitude: String?,
    val longitude: String?,
    val postalCode: String?,
    val language: String?,
    val tenantId: Long,
    val clinicalWorkflows: String,
    val districtId: Long,
    val chiefdomId: Long,
    val isDefault: Boolean = false,
    val linkedVillages:String?
)