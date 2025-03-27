package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.db.entity.PregnancyDetail

@Dao
interface PregnancyDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePregnancyDetail(details: PregnancyDetail): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPregnancyDetails(list: List<PregnancyDetail>)

    @Query("UPDATE PregnancyDetail SET ancVisitNo = :visitCount, lastMenstrualPeriod = :clinicalDate WHERE householdMemberLocalId = :hhmLocalId")
    suspend fun updatePregnancyAnc(visitCount: Long, clinicalDate: String?, hhmLocalId: Long )

    @Query("SELECT patientId, ancVisitNo as visitCount, lastMenstrualPeriod as clinicalDate FROM PregnancyDetail WHERE householdMemberLocalId=:hhmLocalId Limit 1")
    suspend fun getAncDetail(hhmLocalId: Long): MemberClinicalEntity?

    @Query("SELECT pd.patientId, pd.pncVisitNo as visitCount, pd.dateOfDelivery as clinicalDate, pd.noOfNeonates as numberOfNeonate, pd.isDeliveryAtHome, pd.neonateHouseholdMemberLocalId, hhm.isActive as isNeonateAlive, pd.isNeonateDeathRecordedByPHU FROM PregnancyDetail AS pd LEFT JOIN HouseholdMember AS hhm ON pd.neonateHouseholdMemberLocalId = hhm.id WHERE pd.householdMemberLocalId = :hhmLocalId Limit 1")
    suspend fun getPncDetail(hhmLocalId: Long): MemberClinicalEntity?

    @Query("SELECT patientId, childVisitNo as visitCount FROM PregnancyDetail WHERE householdMemberLocalId=:hhmLocalId Limit 1")
    suspend fun getChildhoodVisitDetail(hhmLocalId: Long): MemberClinicalEntity?

    @Query("SELECT * FROM PregnancyDetail WHERE householdMemberLocalId=:hhmLocalId Limit 1")
    suspend fun getPregnancyDetailByPatientId(hhmLocalId: Long): PregnancyDetail?

    @Query("SELECT id from HouseholdMember where fhir_id =:memberId")
    suspend fun getHHMLocalID(memberId: String): Long

    @Query("SELECT id from HouseholdMember where patient_id =:patientId")
    suspend fun getHHMLocalIDByPatientId(patientId: String): Long

    @Query("UPDATE PregnancyDetail SET neonateHouseholdMemberLocalId = :neonateId WHERE householdMemberLocalId = :hhmLocalId")
    suspend fun updateNeonatePatientId(hhmLocalId: Long, neonateId: Long)

    @Transaction
    suspend fun insertOrUpdateFromBE(entity: PregnancyDetail): Long {
        val hhmLocalId = getHHMLocalID(entity.householdMemberId!!)
        val neonateLocalId = entity.neonatePatientId?.let { getHHMLocalIDByPatientId(it) }
        val existingEntity = getPregnancyDetailByPatientId(hhmLocalId)
        val ancVisitNo = existingEntity?.ancVisitNo
        val pncVisitNo = existingEntity?.pncVisitNo
        val childHoodNo = existingEntity?.childVisitNo
        val entityToInsert = existingEntity?.let { entity.copy(id = it.id) } ?: entity
        entityToInsert.householdMemberLocalId = hhmLocalId
        entityToInsert.neonateHouseholdMemberLocalId = neonateLocalId

        entityToInsert.ancVisitNo?.let { existingVisitNo ->
            if (ancVisitNo != null && existingVisitNo < ancVisitNo) {
                entityToInsert.ancVisitNo = ancVisitNo
            }
        }

        entityToInsert.pncVisitNo?.let { existingVisitNo ->
            if (pncVisitNo != null && existingVisitNo < pncVisitNo) {
                entityToInsert.pncVisitNo = pncVisitNo
            }
        }

        entityToInsert.childVisitNo?.let { existingVisitNo ->
            if (childHoodNo != null && existingVisitNo < childHoodNo) {
                entityToInsert.childVisitNo = childHoodNo
            }
        }

        return savePregnancyDetail(entityToInsert)
    }

    @Query("DELETE from PregnancyDetail")
    suspend fun deleteAllPregnancyDetails()

    /*###################################################################*/






    @Query("SELECT neonateHouseholdMemberLocalId FROM PregnancyDetail where householdMemberLocalId =:parentId")
    suspend fun getChildPatientId(parentId: Long): Long?


}