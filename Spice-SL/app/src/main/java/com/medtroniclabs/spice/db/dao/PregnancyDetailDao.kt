package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.db.entity.PregnancyDetail

@Dao
interface PregnancyDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePregnancyDetail(details: PregnancyDetail): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPregnancyDetails(list: List<PregnancyDetail>)

    @Query("SELECT * FROM PregnancyDetail WHERE patientId=:patientId Limit 1")
    suspend fun getPregnancyDetailByPatientId(patientId: String): PregnancyDetail?

    @Query("SELECT patientId, ancVisitNo as visitCount, lastMenstrualPeriod as clinicalDate FROM PregnancyDetail WHERE patientId=:patientId Limit 1")
    suspend fun getAncDetail(patientId: String): MemberClinicalEntity?

    @Query("SELECT patientId, pncVisitNo as visitCount, dateOfDelivery as clinicalDate, noOfNeonates as numberOfNeonate FROM PregnancyDetail WHERE patientId=:patientId Limit 1")
    suspend fun getPncDetail(patientId: String): MemberClinicalEntity?

    @Query("SELECT patientId, childVisitNo as visitCount FROM PregnancyDetail WHERE patientId=:patientId Limit 1")
    suspend fun getChildhoodVisitDetail(patientId: String): MemberClinicalEntity?

    @Query("UPDATE PregnancyDetail SET ancVisitNo = :visitCount, lastMenstrualPeriod = :clinicalDate WHERE patientId = :patientId")
    suspend fun updatePregnancyAnc(visitCount: Long, clinicalDate: String?, patientId: String, )

    @Query("UPDATE PregnancyDetail SET neonatePatientId = :neonatePatientId WHERE patientId = :parentPatientId")
    suspend fun updateNeonatePatientId( parentPatientId: String, neonatePatientId: String)

    @Query("SELECT pd.* FROM PregnancyDetail AS pd JOIN HouseholdMember AS hh ON pd.patientId = hh.patient_id WHERE hh.fhir_id=:memberId Limit 1")
    suspend fun getPregnancyDetailByMemberId(memberId: String): PregnancyDetail?

    @Query("SELECT patient_id from HouseholdMember where fhir_id =:memberId")
    suspend fun getPatientId(memberId: String): String

    @Query("SELECT neonatePatientId FROM PregnancyDetail where patientId =:patientId")
    suspend fun getChildPatientId(patientId: String): String?

    @Transaction
    suspend fun insertOrUpdateFromBE(entity: PregnancyDetail): Long {
        val patientId = getPatientId(entity.householdMemberId!!)
        val existingEntity = getPregnancyDetailByPatientId(patientId)
        val entityToInsert = existingEntity?.let { entity.copy(id = it.id) } ?: entity
        entityToInsert.patientId = patientId

        return savePregnancyDetail(entityToInsert)
    }

    @Query("DELETE from PregnancyDetail")
    suspend fun deleteAllPregnancyDetails()
}