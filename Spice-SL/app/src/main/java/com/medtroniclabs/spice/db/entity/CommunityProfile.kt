package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CommunityProfile")
data class CommunityProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val villageId: Long,
    val communityDescription: String?,
    val registeredDate:String?,
    val payload:String?,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
):BaseEntity()
