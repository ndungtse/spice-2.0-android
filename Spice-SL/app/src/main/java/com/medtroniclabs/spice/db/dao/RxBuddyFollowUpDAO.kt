package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.medtroniclabs.spice.db.entity.RxBuddyFollowUpEntity

@Dao
interface RxBuddyFollowUpDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRxBuddyFollowUp(exBuddyFollowUp: RxBuddyFollowUpEntity): Long
}