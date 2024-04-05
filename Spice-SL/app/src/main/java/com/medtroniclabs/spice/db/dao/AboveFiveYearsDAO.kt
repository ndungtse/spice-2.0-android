package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.medtroniclabs.spice.data.ExaminationsComplaintItems

@Dao
interface AboveFiveYearsDAO {

    @Query("SELECT * FROM ExaminationComplaintsEntity WHERE type = :workflow ORDER BY displayOrder ASC")
    suspend fun getSummaryDetailMetaItems(workflow: String) : List<ExaminationsComplaintItems>
}