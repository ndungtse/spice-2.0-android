package org.medtroniclabs.uhis.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.medtroniclabs.uhis.db.entity.MemberAssessmentHistoryEntity

@Dao
interface MemberAssessmentHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemberAssessmentHistory(historyList: List<MemberAssessmentHistoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemberAssessmentHistory(history: MemberAssessmentHistoryEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAssessmentHistory(assessmentHistory: MemberAssessmentHistoryEntity)

    @Query("SELECT * FROM memberassessmenthistory WHERE (memberFhirId = :memberFhirId OR memberId = :memberId) AND visitDate = :visitDate AND serviceProvided = :serviceProvided LIMIT 1")
    suspend fun getAssessmentHistory(
        memberFhirId: String?,
        memberId: Long?,
        visitDate: String?,
        serviceProvided: String?,
    ): MemberAssessmentHistoryEntity?

    @Query("DELETE FROM memberassessmenthistory")
    suspend fun deleteMemberAssessmentHistory()
}
