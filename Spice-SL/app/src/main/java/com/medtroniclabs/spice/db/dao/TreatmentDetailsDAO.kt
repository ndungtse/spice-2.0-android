package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.medtroniclabs.spice.db.entity.TreatmentDetailsEntity

@Dao
interface TreatmentDetailsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatmentDetails(treatmentDetails: TreatmentDetailsEntity): Long

    @Update
    suspend fun updateTreatmentDetails(treatmentDetails: TreatmentDetailsEntity): Int

    @Query("SELECT * FROM treatmentdetailsentity WHERE memberId = :memberId")
    suspend fun getTreatmentDetailsByMemberId(memberId: Long): TreatmentDetailsEntity?
}