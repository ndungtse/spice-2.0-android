package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FollowUp")
data class FollowUp(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val householdId: String,
    val memberId: String,
    val patientId: String,
    val assessmentId: String,
    val patientStatus: String,
    val reason: String,
    val retryAttempts: Int,
    val type: String,
    val encounterType: String,
    val encounterDate: String? = null,
    val nextVisitDate: String? = null,
    val referredSiteId: String? = null,
    val villageId: String
)
