package com.medtroniclabs.spice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import com.medtroniclabs.spice.data.model.HouseholdCardDetail
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.db.response.HouseholdMemberCount
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus

@Dao
interface HouseholdDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouseHold(houseHold: HouseholdEntity): Long

    @Update
    suspend fun updateHouseHold(houseHold: HouseholdEntity)


    @Query("SELECT MAX(household_no) FROM household WHERE village_id = :villageId")
    suspend fun getLastHouseholdNo(villageId: Long): Long?

    @Query("SELECT * FROM (SELECT hh.*, COUNT(hhm.household_id) AS member_count, case when :status == '' then '' when COUNT(hhm.household_id) == hh.no_of_people then 'Finished' " +
            " when COUNT(hhm.household_id) != hh.no_of_people then 'Pending' else '' end as status FROM Household AS hh LEFT JOIN HouseholdMember AS hhm ON hh.id = hhm.household_id WHERE (hh.name LIKE '%' || :searchTerm || '%' OR hh.household_no LIKE :searchTerm) GROUP BY hh.id) as subTable Where status=:status")
    fun getHouseholdsWithFilterLiveData(
        searchTerm: String, status: String): LiveData<List<HouseHoldEntityWithMemberCount>>

    @Query("SELECT * FROM (SELECT hh.*, COUNT(hhm.household_id) AS member_count, case when :status == '' then '' when COUNT(hhm.household_id) == hh.no_of_people then 'Finished' " +
            " when COUNT(hhm.household_id) != hh.no_of_people then 'Pending' else '' end as status FROM Household AS hh LEFT JOIN HouseholdMember AS hhm ON hh.id = hhm.household_id WHERE hh.village_id IN (:ids) AND (hh.name LIKE '%' || :searchTerm || '%' OR hh.household_no LIKE :searchTerm) GROUP BY hh.id) as subTable Where status=:status")
    fun getHouseholdsWithFilterLiveData(
        searchTerm: String,
        status: String,
            ids: List<Long>
    ): LiveData<List<HouseHoldEntityWithMemberCount>>

    @Query("SELECT * FROM HouseHold WHERE id= :houseHoldId")
    suspend fun getHouseHoldDetailsById(houseHoldId: Long): HouseholdEntity

    @Query("SELECT hh.no_of_people as noOfPeople, count(hhm.id) AS memberCount FROM HouseHold AS hh INNER JOIN HouseHoldMember AS hhm ON hhm.household_id = hh.id WHERE household_id =:householdId")
    fun getHouseholdMemberCountLiveData(householdId: Long): LiveData<HouseholdMemberCount>

    @Query("UPDATE HouseHold SET no_of_people =:newNoOfPeople, sync_status =:syncStatus, updated_at =:updatedAt WHERE id =:householdId")
    suspend fun updateHeadCount(householdId: Long, newNoOfPeople: Int, syncStatus: String = OfflineSyncStatus.NotSynced.name, updatedAt: Long = System.currentTimeMillis())
    @Query("SELECT hh.*, ve.name as villageName FROM HouseHold as hh INNER JOIN VillageEntity AS ve ON hh.village_id = ve.id WHERE hh.sync_status =:status")
    suspend fun getAllUnSyncedHouseHolds(status: String = OfflineSyncStatus.NotSynced.name): List<HouseHold>
    @RawQuery
    suspend fun updateFhirId(query: SimpleSQLiteQuery) : Long

    @Query("SELECT COUNT(id) FROM Household WHERE sync_status =:syncStatus OR fhir_id is null")
    suspend fun getUnSyncedCount(syncStatus: String = OfflineSyncStatus.NotSynced.name): Int

    @Query("SELECT id FROM Household WHERE fhir_id =:fhirId")
    suspend fun getHouseholdIdByFhirId(fhirId: String): Long?

    @Query("DELETE FROM Household")
    suspend fun deleteAllHouseholds()

    @Query("SELECT hh.id, hh.name, hh.household_no AS householdNo, hh.landmark, ve.name AS villageName, hh.head_phone_number AS householdHeadPhoneNumber, hh.no_of_people AS memberRegistered, COUNT(hhm.id) AS memberAdded " +
            "FROM Household as hh INNER JOIN VillageEntity as ve ON hh.village_id = ve.id LEFT JOIN HouseholdMember as hhm ON hhm.household_id = hh.id  WHERE hh.id =:id")
    fun getHouseholdCardDetailLiveData(id: Long): LiveData<HouseholdCardDetail>
}