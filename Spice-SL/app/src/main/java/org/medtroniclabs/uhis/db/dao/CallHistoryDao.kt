package org.medtroniclabs.uhis.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.medtroniclabs.uhis.data.offlinesync.model.HouseholdMemberCallRegisterDto
import org.medtroniclabs.uhis.data.offlinesync.utils.OfflineSyncStatus
import org.medtroniclabs.uhis.db.entity.CallHistory

@Dao
interface CallHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callHistory: CallHistory): Long

    @Query("DELETE FROM CallHistory")
    suspend fun deleteAllCallHistory()

    @Query("UPDATE CallHistory SET syncStatus =:syncStatus WHERE referenceId IN (:ids)")
    suspend fun updateInProgress(
        ids: List<String>,
        syncStatus: String,
    )

    @Query("SELECT hhm.fhir_id as memberId, hhm.patient_id as patientId, hhm.villageId, ch.callStartTime as callDate FROM HouseholdMember AS hhm JOIN CallHistory as CH ON hhm.fhir_id = ch.referenceId WHERE ch.syncStatus IN (:status)")
    suspend fun getUnSyncedCallHistoryForHHMLink(status: List<String> = listOf(OfflineSyncStatus.NotSynced.name, OfflineSyncStatus.NetworkError.name)): List<HouseholdMemberCallRegisterDto>
}
