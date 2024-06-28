package com.medtroniclabs.spice.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class MemberClinicalEntity(
    val patientId: String,
    val visitCount: Long,
    val clinicalDate: String?,
    val numberOfNeonate: Long? = null
)