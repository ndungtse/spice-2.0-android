package com.medtroniclabs.spice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.db.response.HouseholdMemberCount
import com.medtroniclabs.spice.offlinesync.model.HouseHold

@Dao
interface HouseholdDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouseHold(houseHold: HouseholdEntity): Long

    @Update
    suspend fun updateHouseHold(houseHold: HouseholdEntity)

    @Transaction
    @Query(
        "SELECT household.*, COUNT(HouseHoldMember.id) AS member_count " +
                "FROM HouseHold " +
                "LEFT JOIN HouseHoldMember ON household.id = HouseHoldMember.household_id " +
                "GROUP BY household.id"
    )
    suspend fun getAllHouseHold(): List<HouseHoldEntityWithMemberCount>


    @Query("SELECT MAX(household_no) FROM household WHERE village_id = :villageId")
    suspend fun getLastHouseholdNo(villageId: Long): Long?

    @Transaction
    @Query(
        "SELECT household.*, COUNT(HouseHoldMember.household_id) AS member_count " +
                "FROM HouseHold " +
                "LEFT JOIN HouseHoldMember ON household.id = HouseHoldMember.household_id " +
                "WHERE household.name LIKE '%' || :searchTerm || '%' OR household.household_no LIKE :searchTerm " +
                "GROUP BY household.id"
    )
    suspend fun searchByHouseholdNameOrNo(searchTerm: String): List<HouseHoldEntityWithMemberCount>

    @Query("SELECT * FROM HouseHold WHERE id= :houseHoldId")
    suspend fun getHouseHoldDetailsById(houseHoldId: Long): HouseholdEntity

    @Query("SELECT hh.no_of_people as noOfPeople, count(hhm.id) AS memberCount FROM HouseHold AS hh INNER JOIN HouseHoldMember AS hhm ON hhm.household_id = hh.id WHERE household_id =:householdId")
    fun getHouseholdMemberCountLiveData(householdId: Long): LiveData<HouseholdMemberCount>

    @Query("UPDATE HouseHold SET no_of_people =:newNoOfPeople, is_synced =:isSynced, updated_at =:updatedAt WHERE id =:householdId")
    suspend fun updateHeadCount(householdId: Long, newNoOfPeople: Int, isSynced: Boolean = false, updatedAt: Long = System.currentTimeMillis())
    @Query("SELECT hh.*, ve.name as villageName FROM HouseHold as hh INNER JOIN VillageEntity as ve ON hh.village_id = ve.id WHERE (hh.fhir_id is null OR hh.is_synced = 0)")
    suspend fun getAllUnSyncedHouseHolds(): List<HouseHold>
    @RawQuery
    suspend fun updateFhirId(query: SimpleSQLiteQuery) : Long
}