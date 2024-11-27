package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LinkedVillageEntity")
data class LinkedVillageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val villageId: Long,
    val tenantId: Long,
    val name: String,
    val villagecode: String? = null,
    val chiefdomId: Long? = null,
    val countryId: Long,
    val districtId: Long? = null,
    var isUserVillage: Boolean = false,
    val chiefdomCode: String? = null,
    val districtCode: String? = null
)
