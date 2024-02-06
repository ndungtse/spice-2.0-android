package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AssessmentEntity")
data class AssessmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val householdId: Long,
    val assessmentType: String,
    val assessmentDetails:String,
    var otherDetails: String? = null,
    var createdAt: Long = System.currentTimeMillis(),
    var userId: Long? = null,
    var isReferred: Boolean = false
)
