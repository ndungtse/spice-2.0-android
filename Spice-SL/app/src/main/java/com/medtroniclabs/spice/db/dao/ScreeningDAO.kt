package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.db.entity.ScreeningEntity

@Dao
interface ScreeningDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreening(screeningEntity: ScreeningEntity): Long

    @Query("SELECT * FROM ScreeningEntity WHERE id = :id")
    suspend fun getScreeningById(id: Long): ScreeningEntity

}