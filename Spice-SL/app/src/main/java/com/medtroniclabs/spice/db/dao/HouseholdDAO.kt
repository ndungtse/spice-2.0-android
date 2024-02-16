package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount

@Dao
interface HouseholdDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouseHold(houseHold: HouseholdEntity): Long

    @Transaction
    @Query(
        "SELECT household.*, COUNT(household_member.id) AS member_count " +
                "FROM household " +
                "LEFT JOIN household_member ON household.id = household_member.household_id " +
                "GROUP BY household.id"
    )
    suspend fun getAllHouseHold(): List<HouseHoldEntityWithMemberCount>


    @Query("SELECT MAX(household_no) FROM household WHERE village_id = :villageId")
    suspend fun getLastHouseholdNo(villageId: Long): Long?

    @Transaction
    @Query(
        "SELECT household.*, COUNT(household_member.household_id) AS member_count " +
                "FROM household " +
                "LEFT JOIN household_member ON household.id = household_member.household_id " +
                "WHERE household.name LIKE '%' || :searchTerm || '%' OR household.household_no LIKE :searchTerm " +
                "GROUP BY household.id"
    )
    suspend fun searchByHouseholdNameOrNo(searchTerm: String): List<HouseHoldEntityWithMemberCount>

    @Query("SELECT * FROM household WHERE id= :houseHoldId")
    suspend fun getHouseHoldDetailsById(houseHoldId: Long): HouseholdEntity

    @Query("UPDATE household SET no_of_people = :newNoOfPeople WHERE id = :householdId")
    suspend fun updateHeadCount(householdId: Long, newNoOfPeople: Int)
}