package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.data.LastCreatedAtAndPatientId
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.model.MemberDobGenderModel
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.data.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus

@Dao
interface MemberDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(memberEntity: HouseholdMemberEntity): Long

    @Query("SELECT * FROM HouseHoldMember WHERE household_id = :houseHoldId")
    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): List<HouseholdMemberEntity>

    @Query("SELECT * FROM HouseHoldMember WHERE id = :memberId")
    suspend fun getMemberDetailsById(memberId: Long): HouseholdMemberEntity

    @Query("SELECT * FROM HouseHoldMember WHERE parentId = :memberId")
    suspend fun getMemberDetailsByParentId(memberId: Long): List<HouseholdMemberEntity>

    @Query("SELECT COUNT(household_id) FROM HouseHoldMember WHERE household_id = :householdId")
    suspend fun getMemberCountPerHouseHold(householdId: Long): Int

    @Query("SELECT hhm.patient_id FROM HouseholdMember AS hhm INNER JOIN household AS hh ON hhm.household_id = hh.id INNER JOIN villageentity AS vil ON hh.village_id = vil.id WHERE vil.id =:villageId ORDER BY hhm.patient_id DESC LIMIT 1")
    suspend fun getLastPatientId(villageId: Long): String?

    @Query("SELECT date_of_birth,gender FROM HouseHoldMember WHERE id = :memberId")
    suspend fun getDobAndGenderById(memberId: Long): MemberDobGenderModel

    @Query("SELECT hhm.*, hh.fhir_id as household_fhir_id, hh.village_id as village_id, ve.name as village_name  FROM HouseHoldMember AS hhm INNER JOIN Household as hh ON hh.id = hhm.household_id INNER JOIN VillageEntity AS ve ON hh.village_id = ve.id WHERE hhm.household_id = :houseHoldId AND hhm.sync_status =:status")
    suspend fun getAllUnSyncedHouseHoldMembers(
        houseHoldId: Long,
        status: String = OfflineSyncStatus.NotSynced.name
    ): List<HouseHoldMember>

    @Query("SELECT hhm.*, hh.fhir_id as household_fhir_id, hh.village_id as village_id, ve.name as village_name FROM HouseHoldMember AS hhm INNER JOIN Household as hh ON hh.id = hhm.household_id INNER JOIN VillageEntity AS ve ON hh.village_id = ve.id WHERE hhm.household_id NOT IN (:ids) AND hhm.sync_status=:status")
    suspend fun getOtherHouseholdMembers(
        ids: List<Long>,
        status: String = OfflineSyncStatus.NotSynced.name
    ): List<HouseHoldMember>

    @Query("SELECT COUNT(id) FROM HouseholdMember where sync_status =:syncStatus OR fhir_id is null")
    suspend fun getUnSyncedCount(syncStatus: String = OfflineSyncStatus.NotSynced.name): Int

    @Query("SELECT id FROM HouseholdMember WHERE fhir_id =:fhirId")
    suspend fun getHouseholdMemberIdByFhirId(fhirId: String): Long?

    @Query("SELECT hhm.name, hhm.gender, hhm.date_of_birth AS dateOfBirth, hhm.patient_id AS patientId, hh.village_id as villageId, hhm.fhir_id AS memberId, hh.fhir_id AS householdId, hhm.id AS id,hhm.household_id AS householdLocalId  FROM HouseholdMember AS hhm INNER JOIN Household AS hh ON hh.id = hhm.household_id WHERE hhm.id=:id")
    suspend fun getAssessmentMemberDetails(id: Long): AssessmentMemberDetails

    @Query("DELETE FROM HouseholdMember")
    suspend fun deleteAllHouseholdMembers()

}