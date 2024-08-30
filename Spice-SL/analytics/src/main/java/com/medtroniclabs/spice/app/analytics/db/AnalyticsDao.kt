package com.medtroniclabs.spice.app.analytics.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AnalyticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: Analytics): Long

    @Query("SELECT * FROM analytics")
    suspend fun getAllAnalytics(): List<Analytics>

    @Query("SELECT parameter FROM analytics WHERE referenceId=:referenceId AND eventType=:eventType")
    suspend fun getParameterByRefIdAndEvent(referenceId: String, eventType: String): String?

}