package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.medtroniclabs.spice.data.offlinesync.model.RxBuddyRegisterDetail
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.CommunityProfile
import com.medtroniclabs.spice.db.entity.RxBuddyDetails

@Dao
interface RxBuddyDetailsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRxBuddyDetails(rxBuddyDetails: RxBuddyDetails): Long

    @Query("SELECT * FROM RxBuddyDetails WHERE patientMemberId = :patientMemberId AND isActive = 1")
    suspend fun getRxBuddyDetailsByPatientMemberId(patientMemberId: String): RxBuddyDetails?

    @Query("SELECT * FROM RxBuddyDetails WHERE patientMemberId = :patientMemberId")
    suspend fun getRxBuddyDetailsByMemberId(patientMemberId: String): RxBuddyDetails?

    @Query("SELECT rx.*, hhm.patient_id as patientId, hh.village_id as villageId, hh.fhir_id as householdId FROM RxBuddyDetails AS rx JOIN HouseholdMember AS hhm ON hhm.fhir_id = rx.patientMemberId JOIN Household AS hh ON hh.id = hhm.household_id WHERE rx.syncStatus IN (:status)")
    suspend fun getAllUnSyncedRxBuddyRegister(
        status: List<String> = listOf(OfflineSyncStatus.NotSynced.name,
            OfflineSyncStatus.NetworkError.name)
    ): List<RxBuddyRegisterDetail>

    @Query("UPDATE RxBuddyDetails SET nextVisitDate = :nextVisitDate WHERE id = :id")
    suspend fun updateNextVisitDate(id: Long, nextVisitDate: String)

    @Query("UPDATE RxBuddyDetails SET rxBuddyId = :rxBuddyId, syncStatus = :status WHERE id = :id")
    suspend fun updateRxBuddyId(id: Long, rxBuddyId: Long, status: String= OfflineSyncStatus.Success.name)

    @Query("UPDATE RxBuddyDetails SET isActive = :status WHERE householdMemberId = :householdMemberId")
    suspend fun updateRxBuddyStatus(householdMemberId: Long, status: Boolean)

    @Transaction
    suspend fun insertOrUpdateFromBE(entity: RxBuddyDetails): Long {
        val existingEntity = getRxBuddyDetailsByMemberId(entity.patientMemberId)
        if (existingEntity?.syncStatus != OfflineSyncStatus.NotSynced) {
            val entityToInsert = existingEntity?.let { entity.copy(id = it.id) } ?: entity
            entityToInsert.rxBuddyId = entity.rxBuddyId
            entityToInsert.syncStatus = existingEntity?.syncStatus ?: OfflineSyncStatus.Success
            return insertRxBuddyDetails(entityToInsert)
        } else {
            return existingEntity.id
        }
    }

    @Query("DELETE FROM RxBuddyDetails")
    suspend fun deleteAllRxBuddyDetails()

    @Query("SELECT COUNT(id) FROM RxBuddyDetails where syncStatus IN (:syncStatus)")
    suspend fun getUnSyncedCount(syncStatus: List<String> = listOf(OfflineSyncStatus.NotSynced.name, OfflineSyncStatus.NetworkError.name)): Int


    @Query("UPDATE RxBuddyDetails SET syncStatus =:syncStatus WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<Long>, syncStatus: String)

    @Query("DELETE FROM RxBuddyDetails WHERE rxBuddyId IN (:ids) OR isActive = 0")
    suspend fun deleteAllDisabledRxBuddies(ids: List<Long>)
}