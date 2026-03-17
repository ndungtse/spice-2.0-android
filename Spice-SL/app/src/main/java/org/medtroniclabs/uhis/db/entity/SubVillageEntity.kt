package org.medtroniclabs.uhis.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "SubVillageEntity",
    indices = [Index(value = ["villageId"], name = "idx_subvillage_villageId")],
)
data class SubVillageEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val code: String? = null,
    val villageId: Long,
)
