package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.data.ExaminationListItems

@Dao
interface ExaminationsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveExaminationsList(diseaseEntityList: ArrayList<ExaminationListItems>)

    @Query("DELETE FROM ExaminationsEntity")
    suspend fun deleteExaminationsList()

    @Query("SELECT * FROM ExaminationsEntity WHERE type = :workflow LIMIT 1")
    suspend fun getExaminationsByType(workflow: String) : ExaminationListItems
}