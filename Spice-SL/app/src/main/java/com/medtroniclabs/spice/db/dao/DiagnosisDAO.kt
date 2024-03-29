package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.data.DiseaseCategoryItems

@Dao
interface DiagnosisDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDiagnosisList(diseaseEntityList: ArrayList<DiseaseCategoryItems>)

    @Query("DELETE FROM DiagnosisEntity")
    suspend fun deleteDiagnosisList()
}