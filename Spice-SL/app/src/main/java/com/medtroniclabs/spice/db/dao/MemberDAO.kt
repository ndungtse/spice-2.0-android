package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.data.LastCreatedAtAndPatientId
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.model.MemberDobGenderModel
import com.medtroniclabs.spice.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.offlinesync.utils.OfflineSyncStatus

@Dao
interface MemberDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(memberEntity: HouseholdMemberEntity): Long

    @Query("SELECT * FROM HouseHoldMember WHERE household_id = :houseHoldId")
    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): List<HouseholdMemberEntity>

    @Query("SELECT * FROM HouseHoldMember WHERE id = :memberId")
    suspend fun getMemberDetailsById(memberId: Long): HouseholdMemberEntity

    @Query("SELECT COUNT(household_id) FROM HouseHoldMember WHERE household_id = :householdId")
    suspend fun getMemberCountPerHouseHold(householdId: Long): Int

    @Query("SELECT created_at as lastCreatedAt, patient_id AS lastPatientId FROM HouseHoldMember ORDER BY id desc LIMIT 1")
    suspend fun getLastPatientId(): LastCreatedAtAndPatientId?

    @Query("SELECT date_of_birth,gender FROM HouseHoldMember WHERE id = :memberId")
    suspend fun getDobAndGenderById(memberId: Long): MemberDobGenderModel

    @Query("SELECT hhm.*, hh.fhir_id as household_fhir_id FROM HouseHoldMember AS hhm INNER JOIN Household as hh ON hh.id = hhm.household_id WHERE hhm.household_id = :houseHoldId AND hhm.sync_status =:status")
    suspend fun getAllUnSyncedHouseHoldMembers(houseHoldId: Long, status: String = OfflineSyncStatus.NotSynced.name): List<HouseHoldMember>

    @Query("SELECT hhm.*, hh.fhir_id as household_fhir_id FROM HouseHoldMember AS hhm INNER JOIN Household as hh ON hh.id = hhm.household_id WHERE hhm.household_id NOT IN (:ids) AND hhm.sync_status=:status")
    suspend fun getOtherHouseholdMembers(ids: List<Long>, status: String =  OfflineSyncStatus.NotSynced.name): List<HouseHoldMember>

    @Query("SELECT COUNT(id) FROM HouseholdMember where sync_status =:syncStatus OR fhir_id is null")
    suspend fun getUnSyncedCount(syncStatus: String = OfflineSyncStatus.NotSynced.name): Int

}