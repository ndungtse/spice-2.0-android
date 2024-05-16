package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity

@Dao
interface MemberClinicalDAO {

    @Query("SELECT * FROM MemberClinical WHERE type=:type AND patient_id=:patientId Limit 1")
    suspend fun getPatientVisitCountByType(type: String, patientId: String): MemberClinicalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePatientVisitCountByType(memberClinicalEntity: MemberClinicalEntity)

    @Query("UPDATE MemberClinical SET visitcount = :visitCount, clinicalDate = :clinicalDate WHERE patient_id = :patientId AND type =:type")
    suspend fun updateMemberClinicalData(
        visitCount: Long,
        clinicalDate: String?,
        patientId: String,
        type: String
    )

    @Query("DELETE from MemberClinical")
    suspend fun deleteAllMemberClinical()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicalInfos(list: List<MemberClinicalEntity>)

}