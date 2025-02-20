package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.medtroniclabs.spice.db.entity.CommunityDetailsEntity

@Dao
interface CommunityDetailsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunity(communityEntity: CommunityDetailsEntity)

    @Query("SELECT * FROM CommunityDetailsEntity WHERE villageId = :villageId")
    suspend fun getCommunityDetailsById(villageId: Long): CommunityDetailsEntity?

    @Update
    suspend fun updateCommunity(communityEntity: CommunityDetailsEntity)

    @Query("SELECT COUNT(*) FROM CommunityDetailsEntity WHERE villageId = :villageId")
    suspend fun isCommunityExists(villageId: Long): Int

    @Query("UPDATE CommunityDetailsEntity SET sync_status =:syncStatus, updated_at =:updatedAt WHERE villageId = :villageId")
    suspend fun updateSyncStatus(villageId: Long, syncStatus: String, updatedAt: Long = System.currentTimeMillis())

}