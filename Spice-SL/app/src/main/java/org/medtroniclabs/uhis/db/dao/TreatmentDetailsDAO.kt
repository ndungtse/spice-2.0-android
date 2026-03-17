package org.medtroniclabs.uhis.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.medtroniclabs.uhis.db.entity.TreatmentDetailsEntity

@Dao
interface TreatmentDetailsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatmentDetails(treatmentDetails: TreatmentDetailsEntity): Long

    @Update
    suspend fun updateTreatmentDetails(treatmentDetails: TreatmentDetailsEntity): Int

    @Query("SELECT * FROM TreatmentDetailsEntity WHERE memberId = :memberId")
    suspend fun getTreatmentDetailsByMemberId(memberId: String): TreatmentDetailsEntity?

    @Query("DELETE FROM TreatmentDetailsEntity")
    suspend fun deleteAllTreatmentDetails()
}
