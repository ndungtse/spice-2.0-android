package com.medtroniclabs.spice.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ProgramEntity")
data class ProgramEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    @ColumnInfo(name = "health_facilities")
    val healthFacilities: ArrayList<HealthFacility>,
    @ColumnInfo(name = "tenant_id")
    val tenantId: Long
)
