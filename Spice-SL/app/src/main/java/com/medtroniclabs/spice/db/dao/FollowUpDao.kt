package com.medtroniclabs.spice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.data.offlinesync.model.RequestFollowUp
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.FollowUp
import com.medtroniclabs.spice.db.entity.HouseholdEntity

@Dao
interface FollowUpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUp): Long
    @Insert
    suspend fun insertFollowUps(followUpList: List<FollowUp>)

    @Update
    suspend fun updateFollowUps(followUp: FollowUp)

    @Query("DELETE FROM FollowUp")
    suspend fun deleteAllFollowUps()
    @Query("SELECT * FROM FollowUp WHERE id = :id")
    suspend fun getFollowUpDetailsById(id: Long): FollowUp

    @Query("SELECT fu.id, hhm.id AS localPatientId, hhm.name, fu.patientId, hhm.phone_number as phoneNumber, hhm.date_of_birth as dateOfBirth, hhm.gender, fu.reason, fu.patientStatus, ve.name AS village, hh.name AS householdName, hh.landmark, fu.type, fu.encounterType, fu.calledAt, fu.successfulAttempts, fu.unsuccessfulAttempts, fu.nextVisitDate, fu.encounterDate, fu.updatedAt " +
            "FROM FollowUp AS fu INNER JOIN HouseholdMember AS hhm ON fu.memberId = hhm.fhir_id INNER JOIN Household AS hh ON hhm.household_id = hh.id INNER JOIN VillageEntity AS ve ON hh.village_id = ve.id " +
            "WHERE fu.isCompleted = 0 AND fu.id IS NOT NULL AND hh.village_id IN (:villageIds) AND " +
            "fu.type=:type AND " +
            "(hhm.name LIKE '%' || :search || '%' OR fu.patientId LIKE :search || '%' OR :search IS NULL) AND " +
            "CASE WHEN :fromDate = '' THEN 1 ELSE date(fu.encounterDate) BETWEEN :fromDate AND :toDate END ORDER BY fu.encounterDate")
    fun getReferredFollowUpPatientListLiveData(
        type: String,
        search: String? = null,
        villageIds: List<Long> = listOf(),
        fromDate: String = "",
        toDate: String = ""
    ): LiveData<List<FollowUpPatientModel>>


    @Query("SELECT fu.id, hhm.id AS localPatientId, hhm.name, fu.patientId, hhm.phone_number as phoneNumber, hhm.date_of_birth as dateOfBirth, hhm.gender, fu.reason, fu.patientStatus, ve.name AS village, hh.name AS householdName, hh.landmark, fu.type, fu.encounterType, fu.calledAt, fu.successfulAttempts, fu.unsuccessfulAttempts, fu.nextVisitDate, fu.encounterDate, fu.updatedAt " +
            "FROM FollowUp AS fu INNER JOIN HouseholdMember AS hhm ON fu.memberId = hhm.fhir_id INNER JOIN Household AS hh ON hhm.household_id = hh.id INNER JOIN VillageEntity AS ve ON hh.village_id = ve.id " +
            "WHERE fu.isCompleted = 0 AND fu.id IS NOT NULL AND hh.village_id IN (:villageIds) AND " +
            "fu.type=:type AND " +
            "(hhm.name LIKE '%' || :search || '%' OR fu.patientId LIKE :search || '%' OR :search IS NULL) AND " +
            "CASE WHEN :fromDate = '' THEN 1 ELSE date(fu.nextVisitDate) BETWEEN :fromDate AND :toDate END ORDER BY fu.nextVisitDate")
    fun getOtherFollowUpPatientListLiveData(
        type: String,
        search: String? = null,
        villageIds: List<Long> = listOf(),
        fromDate: String = "",
        toDate: String = ""
    ): LiveData<List<FollowUpPatientModel>>

    @Query("SELECT * FROM FollowUp WHERE syncStatus =:syncStatus")
    suspend fun getAllFollowUps(syncStatus: String = OfflineSyncStatus.NotSynced.name): List<FollowUp>

    @Query("SELECT COUNT(referenceId) FROM FollowUp where syncStatus =:syncStatus")
    suspend fun getUnSyncedCount(syncStatus: String = OfflineSyncStatus.NotSynced.name): Int

    @Query("UPDATE FollowUp SET isCompleted = 1 WHERE memberId = :fhirId AND id != :id  AND " +
            "CASE WHEN :type = 'HH_VISIT' THEN (encounterType = :encounterType AND reason= :reason) ELSE encounterType= :encounterType END")
    suspend fun updateOtherDuplicateTickets(id: Long, fhirId: String, type: String, encounterType: String?=null, reason: String? = null)

    @Query("UPDATE FollowUp SET updatedAt = :updateAt, calledAt = :updateAt WHERE memberId = :fhirId AND id != :id  AND " +
            "CASE WHEN :type = 'HH_VISIT' THEN (encounterType = :encounterType AND reason= :reason) ELSE encounterType= :encounterType END")
    suspend fun updateOnTreatmentStatus(id: Long, fhirId: String, type: String,  updateAt:Long, encounterType: String?=null, reason: String? = null)

}