package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.medtroniclabs.spice.db.entity.RxBuddyDetails

@Dao
interface RxBuddyDetailsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRxBuddyDetails(rxBuddyDetails: RxBuddyDetails): Long

    @Query("SELECT * FROM RxBuddyDetails WHERE patientMemberId = :patientMemberId")
    suspend fun getRxBuddyDetailsByPatientMemberId(patientMemberId: String): RxBuddyDetails?
}