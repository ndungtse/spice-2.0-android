package com.medtroniclabs.spice.db.dao

import androidx.lifecycle.LiveData
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

    @Query("SELECT count(id) FROM ScreeningEntity WHERE userId=:userId AND createdAt BETWEEN :startDate AND :endDate")
    fun getScreenedPatientCount(
        startDate: Long,
        endDate: Long,
        userId: Long
    ): LiveData<Long>

    @Query("SELECT count(id) FROM ScreeningEntity WHERE userId=:userId AND isReferred = :isReferred AND createdAt BETWEEN :startDate AND :endDate")
    fun getScreenedPatientReferredCount(
        startDate: Long,
        endDate: Long,
        userId: Long,
        isReferred: Boolean
    ): LiveData<Long>

    @Query("SELECT * FROM ScreeningEntity WHERE uploadStatus = :uploadStatus ORDER BY createdAt DESC")
    suspend fun getAllScreeningRecords(uploadStatus: Boolean): List<ScreeningEntity>

    @Query("DELETE FROM ScreeningEntity WHERE uploadStatus = 1 AND createdAt < :dateTime")
    suspend fun deleteUploadedScreeningRecords(dateTime: Long)

    @Query("UPDATE ScreeningEntity SET uploadStatus = :uploadStatus WHERE id = :id")
    suspend fun updateScreeningRecordById(id: Long, uploadStatus: Boolean)
}