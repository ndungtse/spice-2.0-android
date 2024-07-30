package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.model.assessment.AssessmentDetails

@Dao
interface AssessmentDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(memberEntity: AssessmentEntity): Long

    @Update
    suspend fun updateOtherAssessmentDetails(assessmentEntity: AssessmentEntity)

    @Query("SELECT * FROM Assessment WHERE memberId = :memberId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestAssessmentForMember(memberId: Long): AssessmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptoms(symptomEntity: List<SignsAndSymptomsEntity>)

    @Query("SELECT * FROM SymptomEntity WHERE LOWER(type) = LOWER(:type) ORDER BY display_order")
    suspend fun getSymptomListByType(type: String): List<SignsAndSymptomsEntity>

    @Query("SELECT a.id, a.villageId, a.assessmentType, a.assessmentDetails, a.patientId, a.referralStatus, a.referredReason, a.otherDetails, a.memberId, a.householdId, a.isReferred, a.created_at AS createdAt, a.followUpId, a.latitude, a.longitude " +
                "FROM Assessment AS a LEFT JOIN PregnancyDetail AS pd ON a.patientId = pd.patientId " +
                "WHERE a.sync_status=:status AND a.patientId =:patientId AND a.memberId IS NULL")
    suspend fun getUnSyncedAssessmentByPatientId(
        patientId: String,
        status: String = OfflineSyncStatus.NotSynced.name
    ): List<AssessmentDetails>

    @Query("SELECT a.id, a.villageId, a.assessmentType, a.assessmentDetails, a.patientId, a.referralStatus, a.referredReason, a.otherDetails, hhm.fhir_id as memberId, hh.fhir_id as householdId, a.isReferred, a.created_at AS createdAt, a.followUpId, a.latitude, a.longitude " +
                "FROM Assessment AS a INNER JOIN HouseholdMember AS hhm ON a.patientId = hhm.patient_id INNER JOIN Household AS hh ON hhm.household_id = hh.id " +
                "WHERE a.id NOT IN (:addedAssessmentIds) AND hhm.fhir_id IS NOT NULL AND hhm.fhir_id IS NOT NULL AND a.sync_status=:status")
    suspend fun getOtherUnSyncedAssessments(
        addedAssessmentIds: List<String>,
        status: String = OfflineSyncStatus.NotSynced.name
    ): List<AssessmentDetails>

    @Query("SELECT COUNT(id) FROM Assessment where sync_status =:syncStatus")
    suspend fun getUnSyncedCount(syncStatus: String = OfflineSyncStatus.NotSynced.name): Int

    @Query("DELETE FROM Assessment")
    suspend fun deleteAllAssessments()

    @Query("UPDATE Assessment SET sync_status =:syncStatus, updated_at =:updatedAt WHERE id IN (:ids)")
    suspend fun updateInProgress(ids: List<String>, syncStatus: String = OfflineSyncStatus.InProgress.name, updatedAt: Long = System.currentTimeMillis())


}