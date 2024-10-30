package com.medtroniclabs.spice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.Assigned
import com.medtroniclabs.spice.data.offlinesync.model.UnAssignedHouseholdMemberDetail
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.LinkHouseholdMember

@Dao
interface LinkHouseholdMemberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(linkHHM: LinkHouseholdMember): Long

    @Query("DELETE FROM LinkHouseholdMember WHERE memberId IN (:memberIds)")
    suspend fun delete(memberIds: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(linkHHM: List<LinkHouseholdMember>)

    @Query("DELETE FROM LinkHouseholdMember")
    suspend fun deleteAllLinkHouseholdMember()

    @Query("SELECT l.memberId,hhm.id as lMemberId, hhm.name, hhm.phone_number as phoneNumber, hhm.date_of_birth as dateOfBirth, hhm.gender, hhm.villageId as villageId, v.name as villageName FROM HouseholdMember as hhm JOIN LinkHouseholdMember as l ON hhm.fhir_id = l.memberId JOIN VillageEntity as v ON v.id = hhm.villageId WHERE l.status = :status")
    fun getUnAssignedHouseholdMembersLiveData(status: String = DefinedParams.UnAssigned): LiveData<List<UnAssignedHouseholdMemberDetail>>

    @Query("UPDATE LinkHouseholdMember SET syncStatus =:syncStatus WHERE memberId IN (:ids)")
    suspend fun updateInProgress(ids: List<String>, syncStatus: String)

    @Query("UPDATE LinkHouseholdMember SET status = :status, syncStatus = :syncStatus WHERE memberId = :memberId")
    suspend fun updateMemberAsAssigned(memberId: String, status: String = Assigned, syncStatus: OfflineSyncStatus = OfflineSyncStatus.NotSynced)
}