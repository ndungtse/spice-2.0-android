package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "CommunityDetailsEntity")
data class CommunityDetailsEntity(
    @PrimaryKey
    val villageId: Long,
    val communityDescription: String?,
    val registeredDate:String?,
    val payload:String?,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
):BaseEntity()
