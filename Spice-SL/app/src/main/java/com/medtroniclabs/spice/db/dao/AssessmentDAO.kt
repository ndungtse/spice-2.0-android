package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus

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

    @Query("SELECT * FROM Assessment WHERE sync_status=:status AND patientId =:patientId AND memberId IS NULL")
    suspend fun getUnSyncedAssessmentByPatientId(
        patientId: String,
        status: String = OfflineSyncStatus.NotSynced.name
    ): List<AssessmentEntity>

    @Query("SELECT * FROM Assessment WHERE memberId IS NOT NULL AND householdId IS NOT NULL AND sync_status=:status")
    suspend fun getOtherUnSyncedAssessments(
        status: String = OfflineSyncStatus.NotSynced.name
    ): List<AssessmentEntity>

    @Query("SELECT COUNT(id) FROM Assessment where sync_status =:syncStatus OR fhir_id is null")
    suspend fun getUnSyncedCount(syncStatus: String = OfflineSyncStatus.NotSynced.name): Int

}