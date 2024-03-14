package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.data.LastCreatedAtAndPatientId
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.model.MemberDobGenderModel
import com.medtroniclabs.spice.offlinesync.model.HouseHoldMember

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

    @Query("SELECT MAX(created_at) AS lastCreatedAt, patient_id AS lastPatientId FROM HouseHoldMember")
    suspend fun getLastPatientId(): LastCreatedAtAndPatientId

    @Query("SELECT date_of_birth,gender FROM HouseHoldMember WHERE id = :memberId")
    suspend fun getDobAndGenderById(memberId: Long): MemberDobGenderModel

    @Query("SELECT * FROM HouseHoldMember WHERE household_id = :houseHoldId AND (fhir_id is null OR is_synced = 0)")
    suspend fun getAllUnSyncedHouseHoldMembers(houseHoldId: Long): List<HouseHoldMember>

    @Query("SELECT * FROM HouseHoldMember WHERE household_id NOT IN (:ids) AND (fhir_id is null OR is_synced = 0)")
    suspend fun getOtherHouseholdMembers(ids: List<Long>): List<HouseHoldMember>

}