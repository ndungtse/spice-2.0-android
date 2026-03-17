package org.medtroniclabs.uhis.db.dao

import androidx.room.Dao
import androidx.room.Query
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems

@Dao
interface AboveFiveYearsDAO {
    @Query("SELECT * FROM MetaItemByTypeAndCategoryEntity WHERE type = :workflow ORDER BY displayOrder ASC")
    suspend fun getSummaryDetailMetaItems(workflow: String): List<MedicalReviewMetaItems>
}
