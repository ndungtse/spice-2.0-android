package com.medtroniclabs.spice.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity

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


    @Query("SELECT * FROM MenuEntity")
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
    suspend fun saveClinicalWorkflow(clinicalWorkflowEntity: ClinicalWorkflowEntity)

    @Query("DELETE FROM ClinicalWorkflowEntity")
    suspend fun deleteAllClinicalWorkflow()


    @Query("SELECT id FROM ClinicalWorkflowEntity")
    suspend fun getAllClinicalWorkflowIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveForm(form: FormEntity)

    @Query("DELETE FROM FormEntity")
    suspend fun deleteAllForms()

    @Query("SELECT formInput FROM FormEntity where formType =:formType")
    suspend fun getFormData(
        formType: String
    ): String

    @Query("SELECT formInput FROM FormEntity where formType =:formType AND workflowName =:workflowName")
    suspend fun getFormDataForWorkFlow(formType: String, workflowName: String): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptoms(symptomEntity: SignsAndSymptomsEntity)

    @Query("DELETE FROM SymptomEntity")
    suspend fun deleteAllSymptoms()

    @Query("SELECT * FROM VillageEntity ORDER BY name ASC")
    suspend fun getVillages(): List<VillageEntity>
}