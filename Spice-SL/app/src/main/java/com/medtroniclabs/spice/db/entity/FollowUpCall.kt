package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.medtroniclabs.spice.data.offlinesync.model.FollowUpCallStatus

@Entity(tableName = "FollowUpCall")
data class FollowUpCall(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val followUpId: Long,
    val callDate: String,
    val duration: Long,
    val attempts: Int = 0,
    val status: FollowUpCallStatus = FollowUpCallStatus.UNSUCCESSFUL,
    val patientStatus: String? = null,
    val reason: String? = null,
    val latitude: Double = 0.0,
    val longitude:Double = 0.0
)
