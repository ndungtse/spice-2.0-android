package com.medtroniclabs.spice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.data.ExaminationsComplaintItems

@Dao
interface ExaminationsComplaintsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExaminationsComplaints(entityList: List<ExaminationsComplaintItems>)

    @Query("DELETE FROM ExaminationComplaintsEntity WHERE type = :menuType")
    suspend fun deleteExaminationsComplaints(menuType: String)

    @Query("SELECT * FROM ExaminationComplaintsEntity WHERE type = :workflow ORDER BY displayOrder ASC")
    suspend fun getExaminationsComplaintByType(workflow: String) : List<ExaminationsComplaintItems>

    @Query("DELETE FROM ExaminationComplaintsEntity WHERE type = :type")
    suspend fun deleteExaminationsComplaintsForAnc(type: String)

    @Query("SELECT * FROM examinationcomplaintsentity where category = :category")
    fun getExaminationsComplaintsForAnc(
        category: String
    ): LiveData<List<ExaminationsComplaintItems>>

}