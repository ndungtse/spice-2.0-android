package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.medtroniclabs.spice.data.MedicalReviewMetaItems

@Dao
interface HivMetaDataDAO {
    @Query("SELECT * FROM MetaItemByTypeAndCategoryEntity")
    suspend fun getHivMetaItems(): List<MedicalReviewMetaItems>
}
