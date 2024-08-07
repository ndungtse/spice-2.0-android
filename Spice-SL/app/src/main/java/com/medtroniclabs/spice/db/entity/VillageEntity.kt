package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "VillageEntity")
data class VillageEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val villagecode: String,
    val chiefdomId: Long? = null,
    val countryId: Long,
    val districtId: Long? = null,
    val chiefdomCode: String? = null,
    val districtCode: String? = null
)