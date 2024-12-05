package com.medtroniclabs.spice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.db.entity.NCDCallDetails
import com.medtroniclabs.spice.db.entity.NCDFollowUp
import com.medtroniclabs.spice.db.entity.NCDPatientDetailsEntity

@Dao
interface NCDFollowUpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNCDFollowUp(followUp: NCDFollowUp): Long

    @Query("DELETE FROM NCDFollowUp")
    suspend fun deleteAllNCDFollowUps()

    @Query("""
    SELECT * 
    FROM NCDFollowUp 
    WHERE
        (:villageIds IS NULL OR villageId IN (:villageIds))
        AND LOWER(type) = LOWER(:type)
        AND (
            (:searchText = '' OR :searchText IS NULL) 
            OR (
                LOWER(identityValue) LIKE '%' || LOWER(:searchText) || '%' 
                OR LOWER(name) LIKE '%' || LOWER(:searchText) || '%' 
                OR phoneNumber LIKE '%' || :searchText || '%'
            )
        )
        AND isCompleted != 1
        AND isWrongNumber != 1
        AND deleted != 1
        AND (
            (:dateFirst IS NULL OR dueDate >= :dateFirst) 
            AND (:dateSecond IS NULL OR dueDate <= :dateSecond)
        )

    ORDER BY 
        retryAttempts DESC, 
        dueDate DESC, 
        patientId DESC
""")
    fun getFilteredNCDFollowUp(
        villageIds: List<String>?,
        type: String,
        searchText: String,
        dateFirst: Long?,
        dateSecond: Long?
    ): LiveData<List<NCDFollowUp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatedCallInitiated(id: NCDFollowUp): Long

    @Query("SELECT * FROM NCDFollowUp WHERE id = :id")
    suspend fun getNCDFollowUpById(id: Long): NCDFollowUp

    @Query("SELECT * FROM NCDFollowUp WHERE isInitiated = 1")
    suspend fun getNCDInitiatedCallFollowUp(): NCDFollowUp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNCDCallDetails(followUp: NCDCallDetails): Long

    @Query("UPDATE NCDFollowUp SET isInitiated = 0 where id = :id")
    suspend fun updateNCDInitiatedCallFollowUp(id: Long)
    @Query("SELECT * FROM NCDCallDetails WHERE localId = :id")
    suspend fun getNCDCallDetails(id: Long): NCDCallDetails?

    @Query("UPDATE NCDFollowUp SET isWrongNumber = 1 WHERE id = :id")
    suspend fun markAsWrongNumber(id: Long)

    // don't use this
    @Query("UPDATE NCDFollowUp SET retryAttempts = :retryAttempts,isCompleted = CASE WHEN :retryAttempts = 0 THEN 1 ELSE isCompleted END WHERE id = :id")
    suspend fun updateRetryAttempts(id: Long, retryAttempts: Long)

    @Query("SELECT attempts FROM NCDCallDetails WHERE id = :id ORDER BY localId DESC  LIMIT 1")
    suspend fun getAttemptsById(id: Long): Long?

    @Query("SELECT * FROM NCDCallDetails WHERE isSynced = 0")
    suspend fun getAllNCDCallDetails(): List<NCDCallDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNCDPatientDetails(patients: NCDPatientDetailsEntity): Long

    @Query("DELETE FROM NCDPatientDetailsEntity")
    suspend fun deleteAllNCDPatientDetails()

    @Query("SELECT * FROM NCDPatientDetailsEntity WHERE id = :id")
    suspend fun getPatientBasedOnId(id: String): NCDPatientDetailsEntity

    @Query("DELETE FROM NCDCallDetails WHERE id = :id")
    suspend fun deleteCallDetails(id: Long)
}