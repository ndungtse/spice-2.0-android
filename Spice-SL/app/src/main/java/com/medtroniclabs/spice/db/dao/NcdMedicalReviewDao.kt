package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.db.entity.ComorbidityEntity
import com.medtroniclabs.spice.db.entity.ComplaintsEntity
import com.medtroniclabs.spice.db.entity.ComplicationsEntity
import com.medtroniclabs.spice.db.entity.CurrentMedicationEntity
import com.medtroniclabs.spice.db.entity.LifestyleEntity
import com.medtroniclabs.spice.db.entity.PhysicalExaminationEntity
import com.medtroniclabs.spice.db.entity.TreatmentPlanEntity
import com.medtroniclabs.spice.db.entity.TreatmentPlanFrequencyEntity

@Dao
interface NcdMedicalReviewDao {
    @Query("DELETE FROM ComorbidityEntity")
    suspend fun deleteComorbidities()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComorbidities(counties: List<ComorbidityEntity>)

    @Query("DELETE FROM ComplicationsEntity")
    suspend fun deleteComplications()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplications(items: List<ComplicationsEntity>)

    @Query("DELETE FROM LifestyleEntity")
    suspend fun deleteLifestyle()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLifestyle(items: List<LifestyleEntity>)

    @Query("DELETE FROM ComplaintsEntity")
    suspend fun deleteComplaints()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaints(items: List<ComplaintsEntity>)

    @Query("DELETE FROM PhysicalExaminationEntity")
    suspend fun deletePhysicalExamination()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhysicalExamination(items: List<PhysicalExaminationEntity>)

    @Query("DELETE FROM CurrentMedicationEntity")
    suspend fun deleteCurrentMedications()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentMedications(items: List<CurrentMedicationEntity>)

    @Query("DELETE FROM TreatmentPlanEntity")
    suspend fun deleteTreatmentPlan()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatmentPlan(items: List<TreatmentPlanEntity>)

    @Query("DELETE FROM TreatmentPlanFrequencyEntity")
    suspend fun deleteTreatmentPlanFrequencies()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatmentPlanFrequencies(items: List<TreatmentPlanFrequencyEntity>)
}