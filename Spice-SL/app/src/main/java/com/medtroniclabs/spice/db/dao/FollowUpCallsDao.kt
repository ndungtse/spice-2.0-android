package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.medtroniclabs.spice.db.entity.FollowUpCall

@Dao
interface FollowUpCallsDao {

    @Insert
    suspend fun insertFollowUpCall(followUpCall: FollowUpCall)

    @Query("SELECT * FROM FollowUpCall WHERE followUpId = :followUpId")
    suspend fun getAllFollowUpCalls(followUpId: Long): List<FollowUpCall>

    @Query("DELETE FROM FollowUpCall")
    suspend fun deleteAllFollowUpCalls()
}