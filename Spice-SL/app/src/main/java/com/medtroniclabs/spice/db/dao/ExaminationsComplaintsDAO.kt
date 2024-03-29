package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.data.ExaminationsComplaintItems

@Dao
interface ExaminationsComplaintsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExaminationsComplaints(entityList: List<ExaminationsComplaintItems>)

    @Query("DELETE FROM ExaminationComplaintsEntity")
    suspend fun deleteExaminationsComplaints()
}