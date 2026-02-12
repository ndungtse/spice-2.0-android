package com.medtroniclabs.spice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.offlinesync.model.HHSignatureDetail
import com.medtroniclabs.spice.data.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberStatus
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberWithTb
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdWithMemberCount
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.model.MemberDobGenderModel
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails

@Dao
interface MemberDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(memberEntity: HouseholdMemberEntity): Long

    @Query("SELECT * FROM HouseHoldMember WHERE household_id = :houseHoldId")
    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): List<HouseholdMemberEntity>

    @Query("SELECT hhm.*, td.diagnoses FROM householdmember AS hhm LEFT JOIN TreatmentDetailsEntity AS td ON hhm.fhir_id = td.memberId WHERE hhm.household_id = :houseHoldId")
    fun getAllHouseHoldMembersLiveData(houseHoldId: Long): LiveData<List<HouseholdMemberWithTb>>

    @Query("SELECT * FROM HouseHoldMember WHERE household_id = :houseHoldId AND isActive =:aliveStatus")
    fun getAliveHouseHoldMembers(
        houseHoldId: Long,
        aliveStatus: Boolean
    ): List<HouseholdMemberEntity>

    @Query("SELECT * FROM HouseHoldMember WHERE id = :memberId")
    suspend fun getMemberDetailsById(memberId: Long): HouseholdMemberEntity

    @Query("SELECT * FROM HouseHoldMember WHERE patient_id = :patientId")
    suspend fun getMemberDetailsByPatientId(patientId: String): HouseholdMemberEntity?

    @Query("SELECT * FROM HouseHoldMember WHERE motherReferenceId = :memberId ORDER BY fhir_id IS NULL, fhir_id ASC")
    suspend fun getMemberDetailsByParentId(memberId: String): List<HouseholdMemberEntity>

    @Query("SELECT id, fhir_id AS fhirId, '' AS signatureName FROM HouseHoldMember WHERE fhir_id IS NOT NULL")
    suspend fun getHHSignatureDetails(): List<HHSignatureDetail>

    @Query("SELECT COUNT(household_id) FROM HouseHoldMember WHERE household_id = :householdId")
    suspend fun getMemberCountPerHouseHold(householdId: Long): Int

    @Query("SELECT patient_id FROM HouseholdMember WHERE patient_id LIKE :patientIdStarts ORDER BY patient_id DESC LIMIT 1")
    suspend fun getLastPatientId(patientIdStarts: String): String?

    @Query("SELECT date_of_birth,gender FROM HouseHoldMember WHERE id = :memberId")
    suspend fun getDobAndGenderById(memberId: Long): MemberDobGenderModel

    @Query("SELECT hhm.*, hh.fhir_id as household_fhir_id, hh.village_id as village_id, ve.name as village_name, hh.sub_village_id as sub_village_id, sv.name as sub_village_name, CASE WHEN lhhm.memberId IS NOT NULL AND lhhm.syncStatus IN (:status) THEN 1 ELSE NULL END AS assignHousehold FROM HouseHoldMember AS hhm INNER JOIN Household as hh ON hh.id = hhm.household_id INNER JOIN VillageEntity AS ve ON hh.village_id = ve.id LEFT JOIN SubVillageEntity AS sv ON hh.sub_village_id = sv.id LEFT JOIN LinkHouseholdMember AS lhhm ON lhhm.memberId = hhm.fhir_id WHERE hhm.id NOT IN (:memberIds) AND (hh.fhir_id IS NULL OR hhm.fhir_id IS NULL) AND hhm.household_id = :houseHoldId AND hhm.sync_status IN (:status)")
    suspend fun getAllUnSyncedHouseHoldMembers(
        houseHoldId: Long,
        memberIds: List<Long>,
        status: List<String> = listOf(
            OfflineSyncStatus.NotSynced.name,
            OfflineSyncStatus.NetworkError.name
        )
    ): List<HouseHoldMember>

    @Query("SELECT hhm.*, hh.fhir_id as household_fhir_id, hh.village_id as village_id, ve.name as village_name, hh.sub_village_id as sub_village_id, sv.name as sub_village_name, CASE WHEN lhhm.memberId IS NOT NULL AND lhhm.syncStatus IN (:status) THEN 1 ELSE NULL END AS assignHousehold FROM HouseHoldMember AS hhm INNER JOIN Household as hh ON hh.id = hhm.household_id INNER JOIN VillageEntity AS ve ON hh.village_id = ve.id LEFT JOIN SubVillageEntity AS sv ON hh.sub_village_id = sv.id LEFT JOIN LinkHouseholdMember AS lhhm ON lhhm.memberId = hhm.fhir_id WHERE hhm.id NOT IN (:memberIds) AND hh.fhir_id IS NOT NULL AND hhm.sync_status IN (:status)")
    suspend fun getOtherHouseholdMembers(
        memberIds: List<String>,
        status: List<String> = listOf(
            OfflineSyncStatus.NotSynced.name,
            OfflineSyncStatus.NetworkError.name
        )
    ): List<HouseHoldMember>

    @Query("SELECT COUNT(id) FROM HouseholdMember where sync_status IN (:syncStatus)")
    suspend fun getUnSyncedCount(
        syncStatus: List<String> = listOf(
            OfflineSyncStatus.NotSynced.name,
            OfflineSyncStatus.NetworkError.name
        )
    ): Int

    @Query("SELECT id FROM HouseholdMember WHERE fhir_id =:fhirId")
    suspend fun getHouseholdMemberIdByFhirId(fhirId: String): Long?

    @Query("SELECT hhm.name, hhm.gender, hhm.date_of_birth AS dateOfBirth, hhm.patient_id AS patientId, hh.village_id as villageId, hhm.fhir_id AS memberId, hh.household_no as householdNo, hh.fhir_id AS householdId, hhm.id AS id,hhm.household_id AS householdLocalId, NULL AS contactTracingStatus, NULL AS isPregnant FROM HouseholdMember AS hhm INNER JOIN Household AS hh ON hh.id = hhm.household_id WHERE hhm.id=:id")
    suspend fun getAssessmentMemberDetails(id: Long): AssessmentMemberDetails

    @Query("DELETE FROM HouseholdMember")
    suspend fun deleteAllHouseholdMembers()

    @Query("SELECT patient_id FROM HouseholdMember WHERE fhir_id =:fhirId")
    suspend fun getPatientIdByFhirId(fhirId: String): String?

    @Query("SELECT patient_id FROM HouseholdMember WHERE id =:id")
    suspend fun getPatientIdById(id: Long): String

    @Query("SELECT * FROM HouseholdMember WHERE fhir_id = :fhirId LIMIT 1")
    suspend fun getByUniqueField(fhirId: String): HouseholdMemberEntity?

    @Transaction
    suspend fun insertOrUpdateFromBE(entity: HouseholdMemberEntity): Long {
        if (!entity.isActive && entity.fhirId != null) {
            deleteRxBuddyOnDeceased(entity.fhirId!!)
        }
        val existingEntity = entity.fhirId?.let { getByUniqueField(it) }
        if (existingEntity?.sync_status != OfflineSyncStatus.NotSynced) {
            val entityToInsert = existingEntity?.let { entity.copy(id = it.id) } ?: entity
            entityToInsert.sync_status = existingEntity?.sync_status ?: OfflineSyncStatus.Success
            entityToInsert.fhirId = entity.fhirId
            return insertMember(entityToInsert)
        } else {
            return existingEntity.id
        }
    }

    @Query("DELETE FROM RxBuddyDetails WHERE patientMemberId = :memberId")
    suspend fun deleteRxBuddyOnDeceased(memberId: String)

    @Query("UPDATE HouseholdMember SET sync_status =:syncStatus, updated_at =:updatedAt WHERE id IN (:memberIds)")
    suspend fun updateInProgress(
        memberIds: List<String>,
        syncStatus: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE HouseholdMember SET sync_status =:syncStatus WHERE id = :id")
    suspend fun changeMemberDetailsToNotSynced(
        id: Long,
        syncStatus: OfflineSyncStatus = OfflineSyncStatus.NotSynced
    )

    @Query("UPDATE HouseholdMember SET isActive = :status, sync_status =:syncStatus  WHERE id = :id")
    suspend fun updateMemberDeceasedStatus(
        id: Long,
        status: Boolean,
        syncStatus: OfflineSyncStatus
    )

    @Query("UPDATE HouseholdMember SET isActive = :status, sync_status =:syncStatus , deceasedReason=:deceasedReason ,updated_at =:updatedAt WHERE id = :id")
    suspend fun updateMemberDeceasedReason(
        id: Long,
        status: Boolean,
        syncStatus: OfflineSyncStatus,
        deceasedReason: String?, updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE householdmember SET phone_number = :phoneNumber, sync_status =:syncStatus, updated_at =:updatedAt  WHERE household_id = :householdId AND is_house_hold_head = 1")
    suspend fun updatePhoneNumberForHouseholdHead(
        householdId: Long,
        phoneNumber: String?,
        syncStatus: String = OfflineSyncStatus.NotSynced.name,
        updatedAt: Long = System.currentTimeMillis()
    )


    @Query("UPDATE HouseholdMember SET household_id = :householdId, sync_status =:syncStatus, updated_at =:updatedAt  WHERE fhir_id IN (:memberIds)")
    suspend fun updateHouseholdHeadAndRelationShip(
        memberIds: List<String>,
        householdId: Long,
        syncStatus: String = OfflineSyncStatus.NotSynced.name,
        updatedAt: Long = System.currentTimeMillis()
    )


    @Query("SELECT date_of_birth FROM HouseholdMember WHERE household_id = :householdId AND is_house_hold_head = 1 LIMIT 1")
    suspend fun getHouseholdHeadDob(
        householdId: Long
    ): String

    @Query("SELECT hm.* FROM HouseholdMember AS hm WHERE hm.household_id = :hhId")
    fun getHouseholdMemberWithTBContactTraceStatus(hhId: Long): LiveData<List<HouseholdMemberEntity>>

    @Query("UPDATE PregnancyDetail SET tbContactTraceStatus = :tbContactTraceStatus WHERE householdMemberLocalId = :householdMemberId")
    suspend fun updateTBContactTraceStatus(householdMemberId: Long, tbContactTraceStatus: Int)

    @Query("UPDATE HouseholdMember SET sync_status =:syncStatus WHERE id = :memberId AND (:isPregnant IS NULL OR :isPregnant IS NOT NULL)")
    suspend fun updatePregnantStatus(memberId: Long, isPregnant: Boolean, syncStatus: String = OfflineSyncStatus.NotSynced.name)

    @Query("SELECT * FROM HouseholdMember WHERE household_id = :householdId AND id != :patientId AND isActive=1 AND substr(date_of_birth, 1, 10) < date('now','-10 years') ")
    suspend fun getOtherHouseholdExcludeTBPatient(
        householdId: Long,
        patientId: Long
    ): List<HouseholdMemberEntity>

    @Query("SELECT hhm.*, hh.fhir_id as household_fhir_id, hh.village_id as village_id, ve.name as village_name FROM HouseHoldMember AS hhm INNER JOIN Household as hh ON hh.id = hhm.household_id INNER JOIN VillageEntity AS ve ON hh.village_id = ve.id Where hhm.id = :hhmId")
    suspend fun getHouseholdMemberForRxBuddy(hhmId: Long): HouseHoldMember

    @Query("SELECT id, isActive FROM HouseholdMember WHERE fhir_id =:fhirId")
    suspend fun getHouseholdMemberIdAndStatusByFhirId(fhirId: String): HouseholdMemberStatus?

    @Query("SELECT hh.id AS id, COUNT(hhm.id) AS hhmCount, hh.no_of_people AS noOfPeople FROM Household AS hh LEFT JOIN HouseholdMember AS hhm ON hh.id = hhm.household_id group by hh.id HAVING hhmCount > noOfPeople")
    suspend fun getHouseholdsWithMemberCountsExceeding(): List<HouseholdWithMemberCount>

    @Query("SELECT fhir_id FROM HouseholdMember WHERE id =:hhmId")
    suspend fun getMemberFhirIdByLocalId(hhmId: Long): String?

    @Query("SELECT hhm.id FROM HouseholdMember AS hhm JOIN TreatmentDetailsEntity AS td ON hhm.fhir_id = td.memberId WHERE hhm.household_id =:householdId")
    suspend fun getTbPatientLocalIdByHouseholdId(householdId: Long): MutableList<Long>

    @Query("UPDATE HouseholdMember SET sync_status =:syncStatus, updated_at =:updatedAt WHERE id = :memberId AND (:status IS NULL OR :status IS NOT NULL)")
    suspend fun updateContactTracingStatus(memberId: Long, status: Int?, syncStatus: String = OfflineSyncStatus.NotSynced.name, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE HouseholdMember SET sync_status =:syncStatus, updated_at =:updatedAt WHERE household_id = :householdId AND id != :tbHHMId")
    suspend fun updateContactTracingForLinkTbPatient(tbHHMId: Long, householdId: Long, syncStatus: String = OfflineSyncStatus.NotSynced.name, updatedAt: Long = System.currentTimeMillis())
}