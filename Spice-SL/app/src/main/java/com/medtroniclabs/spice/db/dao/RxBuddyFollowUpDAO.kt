package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.data.offlinesync.model.RxBuddyFollowUpDetails
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.RxBuddyFollowUpEntity

@Dao
interface RxBuddyFollowUpDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRxBuddyFollowUp(exBuddyFollowUp: RxBuddyFollowUpEntity): Long

    @Query("SELECT * FROM RxBuddyFollowUpEntity WHERE rxBuddyLocalId =:rxBuddyLocalId AND rxBuddyId = 0 AND syncStatus IN (:status)")
    suspend fun getUnSyncedRxBuddyFollowUpWithoutRxBuddyId(
        rxBuddyLocalId: Long,
        status: List<String> = listOf(
            OfflineSyncStatus.NotSynced.name,
            OfflineSyncStatus.NetworkError.name)
    ): List<RxBuddyFollowUpEntity>

    @Query("SELECT rxf.id, rxf.rxBuddyLocalId, rxf.patientMemberId, rxf.followUp, rxf.nextVisitDate, rxf.followUpId, rxf.syncStatus, rxf.latitude, rxf.longitude, rxf.createdBy, rxf.updatedAt, rx.rxBuddyId, hhm.patient_id as patientId, hh.village_id as villageId, hh.fhir_id as householdId FROM RxBuddyFollowUpEntity AS rxf JOIN RxBuddyDetails AS rx ON rx.id = rxf.rxBuddyLocalId JOIN HouseholdMember AS hhm ON hhm.fhir_id = rxf.patientMemberId JOIN Household AS hh ON hh.id = hhm.household_id WHERE rx.rxBuddyId != 0 AND rxf.syncStatus IN (:status)")
    suspend fun getUnSyncedRxBuddyFollowUpWithRxBuddyId(
        status: List<String> = listOf(
            OfflineSyncStatus.NotSynced.name,
            OfflineSyncStatus.NetworkError.name)
    ): List<RxBuddyFollowUpDetails>

    @Query("UPDATE RxBuddyFollowUpEntity SET nextVisitDate = :nextVisitDate WHERE id = :id")
    suspend fun updateNextVisitDate(id: Long, nextVisitDate: String)

    @Query("UPDATE RxBuddyFollowUpEntity SET rxBuddyId = :rxBuddyId, syncStatus = :status WHERE rxBuddyLocalId = :id")
    suspend fun updateRxBuddyId(id: Long, rxBuddyId: Long, status: String= OfflineSyncStatus.Success.name)

    @Query("DELETE FROM RxBuddyFollowUpEntity")
    suspend fun deleteAllRxBuddyFollowUp()

    @Query("SELECT COUNT(id) FROM RxBuddyFollowUpEntity where syncStatus IN (:syncStatus)")
    suspend fun getUnSyncedCount(syncStatus: List<String> = listOf(OfflineSyncStatus.NotSynced.name, OfflineSyncStatus.NetworkError.name)): Int

    @Query("UPDATE RxBuddyFollowUpEntity SET syncStatus =:syncStatus WHERE rxBuddyLocalId IN (:rxBuddyLocalIds)")
    suspend fun updateSyncStatus(rxBuddyLocalIds: List<Long>, syncStatus: String)
}