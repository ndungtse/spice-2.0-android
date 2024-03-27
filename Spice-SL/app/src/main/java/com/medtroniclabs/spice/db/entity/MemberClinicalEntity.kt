package com.medtroniclabs.spice.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = EntitiesName.MEMBER_CLINICAL)
data class MemberClinicalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo("patient_id")
    val patientId: String,
    val type: String,
    val visitCount: Long,
    val clinicalDate: String
)