package org.medtroniclabs.uhis.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.medtroniclabs.uhis.data.ExaminationListItems

@Dao
interface ExaminationsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveExaminationsList(diseaseEntityList: ArrayList<ExaminationListItems>)

    @Query("DELETE FROM ExaminationsEntity WHERE type = :menuType")
    suspend fun deleteExaminationsList(menuType: String)

    @Query("SELECT * FROM ExaminationsEntity WHERE type = :workflow LIMIT 1")
    suspend fun getExaminationsByType(workflow: String): ExaminationListItems
}
