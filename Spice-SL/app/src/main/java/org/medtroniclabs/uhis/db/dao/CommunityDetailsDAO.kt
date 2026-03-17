package org.medtroniclabs.uhis.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.medtroniclabs.uhis.data.offlinesync.utils.OfflineSyncStatus
import org.medtroniclabs.uhis.db.entity.CommunityProfile

@Dao
interface CommunityDetailsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunity(communityEntity: CommunityProfile): Long

    @Query("SELECT * FROM CommunityProfile WHERE villageId = :villageId")
    suspend fun getCommunityDetailsById(villageId: Long): CommunityProfile?

    @Query("SELECT id FROM CommunityProfile WHERE villageId = :villageId")
    suspend fun getCommunityProfileId(villageId: Long): Long?

    @Query("DELETE FROM CommunityProfile")
    suspend fun deleteAllCommunityProfiles()

    @Query("SELECT COUNT(id) FROM CommunityProfile where sync_status IN (:syncStatus)")
    suspend fun getUnSyncedCount(syncStatus: List<String> = listOf(OfflineSyncStatus.NotSynced.name, OfflineSyncStatus.NetworkError.name)): Int

    @Update
    suspend fun updateCommunity(communityEntity: CommunityProfile)

    @Query("UPDATE CommunityProfile SET sync_status =:syncStatus, updated_at =:updatedAt WHERE villageId = :villageId")
    suspend fun updateSyncStatus(
        villageId: Long,
        syncStatus: String,
        updatedAt: Long = System.currentTimeMillis(),
    )

    @Query("SELECT * FROM CommunityProfile WHERE sync_status in (:status)")
    suspend fun getUnSyncedCommunityDetails(status: List<String> = listOf(OfflineSyncStatus.NotSynced.name, OfflineSyncStatus.NetworkError.name)): List<CommunityProfile>

    @Query("UPDATE CommunityProfile SET sync_status =:syncStatus WHERE villageId IN (:ids)")
    suspend fun updateInStatus(
        ids: List<Long>,
        syncStatus: String,
    )

    @Transaction
    suspend fun insertOrUpdateFromBE(entity: CommunityProfile): Long {
        val existingEntity = getCommunityDetailsById(entity.villageId)
        if (existingEntity?.sync_status != OfflineSyncStatus.NotSynced) {
            val entityToInsert = existingEntity?.let { entity.copy(id = it.id) } ?: entity
            entityToInsert.fhirId = entity.fhirId
            entityToInsert.sync_status = existingEntity?.sync_status ?: OfflineSyncStatus.Success
            return insertCommunity(entityToInsert)
        } else {
            return existingEntity.id
        }
    }
}
