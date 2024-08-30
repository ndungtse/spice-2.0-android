package com.medtroniclabs.spice.app.analytics.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils.getCurrentDateTimeInUTC

@Entity(tableName = "analytics")
data class Analytics(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val userId: String,
    val eventType: String,
    val parameter: String,
    val referenceId: String? = null,
    val createdAt: String = getCurrentDateTimeInUTC(),
    var updateAt: String = getCurrentDateTimeInUTC()
)
