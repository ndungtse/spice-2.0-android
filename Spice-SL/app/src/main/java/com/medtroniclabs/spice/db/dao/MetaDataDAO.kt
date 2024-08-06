package com.medtroniclabs.spice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.data.VillageInfo
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowConditionEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntityWithSubmodule
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.response.VillageBasicDetails

@Dao
interface MetaDataDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthFacility(healthFacilityEntityList: HealthFacilityEntity)

    @Query("SELECT * FROM HealthFacilityEntity WHERE isDefault = 1")
    suspend fun getDefaultHealthFacility(): HealthFacilityEntity?

    @Query("DELETE FROM HealthFacilityEntity")
    suspend fun deleteAllHealthFacility()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVillages(villageEntityList: List<VillageEntity>)

    @Query("DELETE FROM VillageEntity")
    suspend fun deleteAllVillages()

    @Query("SELECT * FROM VillageEntity")
    suspend fun getAllVillageName(): List<VillageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenus(menuEntity: MenuEntity)


    @Query("SELECT * FROM MenuEntity  ORDER BY displayOrder ASC")
    suspend fun getMenus(): List<MenuEntity>

    @Query("DELETE FROM MenuEntity")
    suspend fun deleteAllMenus()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfileDetails(userProfileEntity: UserProfileEntity)

    @Query("DELETE FROM UserProfileEntity")
    suspend fun deleteAllUserProfileDetails()

    @Query("SELECT * FROM UserProfileEntity")
    suspend fun getUserProfile(): UserProfileEntity


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveClinicalWorkflows(clinicalWorkflows: List<ClinicalWorkflowEntity>)

    @Query("DELETE FROM ClinicalWorkflowEntity")
    suspend fun deleteAllClinicalWorkflow()


    @Query("SELECT id FROM ClinicalWorkflowEntity")
    suspend fun getAllClinicalWorkflowIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveForms(forms: List<FormEntity>)

    @Query("DELETE FROM FormEntity")
    suspend fun deleteAllForms()

    @Query("SELECT formInput FROM FormEntity where formType =:formType")
    suspend fun getFormData(
        formType: String
    ): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptoms(symptomEntity: List<SignsAndSymptomsEntity>)

    @Query("DELETE FROM SymptomEntity")
    suspend fun deleteAllSymptoms()

    @Query("SELECT * FROM VillageEntity ORDER BY name ASC")
    suspend fun getVillages(): List<VillageEntity>

    @Query("SELECT chiefdomId,villagecode AS code FROM VillageEntity WHERE id =:id")
    suspend fun getChiefDomAndVillageCodeByVillageId(id: Long): VillageInfo

    @Query("SELECT * FROM VillageEntity where id = :villageId")
    suspend fun getVillageByID(villageId: Long): VillageEntity

    @Query("SELECT * FROM ClinicalWorkflowEntity")
    suspend fun getMenuForClinicalWorkflows() :List<ClinicalWorkflowEntity>

    @Query("DELETE FROM ClinicalWorkflowConditionEntity")
    suspend fun deleteClinicalWorkflowConditions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicalWorkflowConditions(clinicalWorkflowConditions: List<ClinicalWorkflowConditionEntity>)

    @Query("SELECT DISTINCT wf.*, wfc.subModule FROM ClinicalWorkflowEntity AS wf LEFT JOIN ClinicalWorkflowConditionEntity AS wfc ON wf.id = wfc.clinicalWorkflowId WHERE (LOWER(wfc.gender) = 'both' OR LOWER(wfc.gender) = LOWER(:gender)) AND (wfc.maxAge IS NULL OR wfc.maxAge > :age) AND (wfc.minAge IS NULL OR wfc.minAge <= :age) AND (wfc.moduleType = :moduleType ) ORDER BY wf.displayOrder")
    suspend fun getClinicalWorkflowId(gender: String, age: Int, moduleType: String): List<ClinicalWorkflowEntityWithSubmodule>

    @Query("SELECT * FROM HealthFacilityEntity Order by isDefault DESC")
    suspend fun getNearestHealthFacility(): List<HealthFacilityEntity>

    @Query("SELECT id,name,districtId FROM VillageEntity")
    suspend fun getVillageIdName(): List<VillageBasicDetails>
    @Query("SELECT id FROM VillageEntity")
    suspend fun getVillageIds(): List<Long>
}