package com.medtroniclabs.spice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.db.entity.FollowUp

@Dao
interface FollowUpDao {

    @Insert
    suspend fun insertFollowUps(followUpList: List<FollowUp>)

    @Query("DELETE FROM FollowUp")
    suspend fun deleteAllFollowUps()

    @Query("SELECT fu.id, hhm.id AS localPatientId, hhm.name, fu.patientId, hhm.date_of_birth as dateOfBirth, hhm.gender, fu.reason, fu.patientStatus, ve.name AS village, hh.name AS householdName, hh.landmark, fu.type, fu.encounterType, fu.retryAttempts AS remainingRetryAttempt, fu.nextVisitDate, fu.encounterDate " +
            "FROM FollowUp AS fu INNER JOIN HouseholdMember AS hhm ON fu.memberId = hhm.fhir_id INNER JOIN Household AS hh ON hhm.household_id = hh.id INNER JOIN VillageEntity AS ve ON hh.village_id = ve.id " +
            "WHERE hh.village_id IN (:villageIds) AND " +
            "fu.type=:type AND " +
            "(hhm.name LIKE '%' || :search || '%' OR :search IS NULL) AND " +
            "CASE WHEN :fromDate = '' THEN 1 ELSE date(fu.nextVisitDate) BETWEEN :fromDate AND :toDate END")
    fun getFollowUpPatientListLiveData(
        type: String,
        search: String? = null,
        villageIds: List<Long> = listOf(),
        fromDate: String = "",
        toDate: String = ""
    ): LiveData<List<FollowUpPatientModel>>

}