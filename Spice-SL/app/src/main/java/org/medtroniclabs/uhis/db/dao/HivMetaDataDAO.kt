package org.medtroniclabs.uhis.db.dao

import androidx.room.Dao
import androidx.room.Query
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems

@Dao
interface HivMetaDataDAO {
    @Query("SELECT * FROM MetaItemByTypeAndCategoryEntity")
    suspend fun getHivMetaItems(): List<MedicalReviewMetaItems>
}
