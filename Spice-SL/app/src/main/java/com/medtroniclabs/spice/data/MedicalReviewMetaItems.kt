package com.medtroniclabs.spice.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MetaItemByTypeAndCategoryEntity")
data class MedicalReviewMetaItems(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long= 0,
    val id: Long,
    var name: String,
    var category: String? = null,
    var type: String? = null,
    val displayOrder: Int,
    @ColumnInfo(name = "culture_value")
    val displayValue: String? = null,
    val value: String? = null
)
