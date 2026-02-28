package com.medtroniclabs.spice.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.spice.data.CulturesEntity
import com.medtroniclabs.spice.data.DosageFrequency
import com.medtroniclabs.spice.data.ProgramEntity
import com.medtroniclabs.spice.data.UnitMetricEntity
import com.medtroniclabs.spice.data.community.CommunityPopulationStatistics
import com.medtroniclabs.spice.data.community.CommunityProfileDetail
import com.medtroniclabs.spice.db.entity.ChiefDomEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowConditionEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.ConsentEntity
import com.medtroniclabs.spice.db.entity.DistrictEntity
import com.medtroniclabs.spice.db.entity.DosageDurationEntity
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.LinkedVillageEntity
import com.medtroniclabs.spice.db.entity.MedicalComplianceEntity
import com.medtroniclabs.spice.db.entity.MentalHealthEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.NCDAssessmentClinicalWorkflow
import com.medtroniclabs.spice.db.entity.ShasthyaShebikaEntity
import com.medtroniclabs.spice.db.entity.ShasthyaShebikaLinkedVillageEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.SubVillageEntity
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

    @Query("SELECT * FROM VillageEntity WHERE chiefdomId =:chiefdomId OR chiefdomId IS NULL ORDER BY name ASC")
    suspend fun getVillagesByChiefDom(chiefdomId: Long): List<VillageEntity>

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
        formType: String,
    ): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptoms(symptomEntity: List<SignsAndSymptomsEntity>)

    @Query("DELETE FROM SymptomEntity")
    suspend fun deleteAllSymptoms()

    @Query("SELECT * FROM VillageEntity ORDER BY name ASC")
    suspend fun getVillages(): List<VillageEntity>

    @Query("SELECT * FROM VillageEntity where id = :villageId")
    suspend fun getVillageByID(villageId: Long): VillageEntity

    @Query("SELECT * FROM ClinicalWorkflowEntity")
    suspend fun getMenuForClinicalWorkflows(): List<ClinicalWorkflowEntity>

    @Query("DELETE FROM ClinicalWorkflowConditionEntity")
    suspend fun deleteClinicalWorkflowConditions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicalWorkflowConditions(clinicalWorkflowConditions: List<ClinicalWorkflowConditionEntity>)

    @Query("SELECT DISTINCT wf.*, wfc.subModule FROM ClinicalWorkflowEntity AS wf LEFT JOIN ClinicalWorkflowConditionEntity AS wfc ON wf.id = wfc.clinicalWorkflowId WHERE (LOWER(wfc.gender) = 'both' OR LOWER(wfc.gender) = LOWER(:gender)) AND (wfc.maxAge IS NULL OR wfc.maxAge > :age) AND (wfc.minAge IS NULL OR wfc.minAge <= :age) AND (wfc.moduleType = :moduleType ) ORDER BY wf.displayOrder")
    suspend fun getClinicalWorkflowId(
        gender: String,
        age: Int,
        moduleType: String,
    ): List<NCDAssessmentClinicalWorkflow>

    @Query("SELECT * FROM HealthFacilityEntity Order by isDefault DESC")
    suspend fun getNearestHealthFacility(): List<HealthFacilityEntity>

    @Query("SELECT id FROM SubVillageEntity")
    suspend fun getVillageIds(): List<Long>

    @Query("SELECT * FROM HealthFacilityEntity Where isUserSite =:isUserSite")
    suspend fun getUserHealthFacility(isUserSite: Boolean): List<HealthFacilityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programEntity: List<ProgramEntity>)

    @Query("SELECT * FROM ProgramEntity")
    suspend fun getPrograms(): List<ProgramEntity>

    @Query("DELETE FROM ProgramEntity")
    suspend fun deletePrograms()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCultures(culturesEntity: List<CulturesEntity>)

    @Query("SELECT * FROM CulturesEntity")
    suspend fun getCultures(): List<CulturesEntity>

    @Query("DELETE FROM CulturesEntity")
    suspend fun deleteCultures()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveForm(forms: FormEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsent(consentEntity: ConsentEntity)

    @Query("SELECT formInput FROM ConsentEntity where formType=:formType")
    fun getConsent(formType: String): LiveData<String>

    @Query("DELETE FROM ConsentEntity")
    suspend fun deleteConsent()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModelQuestions(mentalHealthEntity: List<MentalHealthEntity>)

    @Query("SELECT * FROM MentalHealthEntity where formType=:formType")
    suspend fun getModelQuestions(formType: String): MentalHealthEntity

    @Query("DELETE FROM MentalHealthEntity")
    suspend fun deleteModelQuestions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalCompliance(list: List<MedicalComplianceEntity>)

    @Query("SELECT * FROM MedicalComplianceEntity where parent_compliance_id IS NULL OR parent_compliance_id = '' ORDER BY display_order")
    suspend fun getMedicalComplianceList(): List<MedicalComplianceEntity>

    @Query("SELECT * FROM MedicalComplianceEntity where parent_compliance_id =:parentId ORDER BY display_order")
    suspend fun getMedicalComplianceList(parentId: Long): List<MedicalComplianceEntity>

    @Query("DELETE FROM MedicalComplianceEntity")
    suspend fun deleteMedicalComplianceList()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDistricts(counties: List<DistrictEntity>)

    @Query("SELECT * FROM DistrictEntity where countryId=:countryId ORDER BY name ASC")
    suspend fun getDistricts(countryId: Long): List<DistrictEntity>

    @Query("DELETE FROM DistrictEntity")
    suspend fun deleteCounties()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChiefDoms(counties: List<ChiefDomEntity>)

    @Query("SELECT * FROM ChiefDomEntity where districtId=:districtId ORDER BY name ASC")
    suspend fun getChiefDoms(districtId: Long): List<ChiefDomEntity>

    @Query("DELETE FROM ChiefDomEntity")
    suspend fun deleteChiefDoms()

    @Query("SELECT * FROM MentalHealthEntity where formType=:formType")
    fun getMentalQuestion(formType: String): LiveData<MentalHealthEntity?>

    @Query("SELECT * FROM HealthFacilityEntity Order by isDefault DESC")
    fun getSites(): LiveData<List<HealthFacilityEntity>>

    @Query("SELECT formInput FROM FormEntity where formType =:formType OR formType =:customizedFormType")
    suspend fun getNCDForm(
        formType: String,
        customizedFormType: String,
    ): List<String>

    @Query("SELECT formInput FROM FormEntity where formType IN (:formTypes) AND workflowName =:workFlow")
    fun getAssessmentFormData(
        formTypes: List<String>,
        workFlow: String,
    ): List<String>

    @Query("SELECT formInput FROM FormEntity where formType =:formType AND workflowName =:workFlow")
    fun getAssessmentFormData(
        formType: String,
        workFlow: String,
    ): LiveData<String>

    @Query("SELECT cwe.id, cwe.name, cwe.workflowName, cwce.category, cwce.groupName, cwce.cultureGroupName, cwce.subModule, cwe.displayOrder FROM ClinicalWorkflowEntity AS cwe JOIN ClinicalWorkflowConditionEntity AS cwce ON cwe.id = cwce.clinicalWorkflowId WHERE cwce.gender = :gender AND cwce.moduleType = :moduleType ORDER BY cwe.displayOrder")
    suspend fun getAssessmentClinicalWorkflow(
        gender: String,
        moduleType: String,
    ): List<NCDAssessmentClinicalWorkflow>

    @Query("SELECT * FROM unit_metric_entity where type=:type")
    suspend fun getUnitList(type: String): List<UnitMetricEntity>

    @Query("DELETE FROM unit_metric_entity")
    suspend fun deleteUnitMetric()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDosageFrequencyList(list: ArrayList<DosageFrequency>)

    @Query("SELECT * FROM dosage_frequency_entity ")
    suspend fun getDosageFrequencyList(): List<DosageFrequency>

    @Query("DELETE FROM dosage_frequency_entity")
    suspend fun deleteDosageFrequencyList()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitMetricList(frequencyList: List<UnitMetricEntity>): List<Long>

    @Query("SELECT * FROM VillageEntity Where isUserVillage =:isUserVillage ORDER BY name ASC")
    suspend fun getUserVillages(isUserVillage: Boolean): List<VillageEntity>

    @Query("DELETE FROM DosageDurationEntity")
    suspend fun deleteDosageDurations()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDosageDurations(dosageDurationEntity: List<DosageDurationEntity>)

    @Query("SELECT * FROM DosageDurationEntity")
    suspend fun getDosageDurationsList(): List<DosageDurationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkedVillages(linkedVillages: List<LinkedVillageEntity>)

    @Query("DELETE FROM LinkedVillageEntity")
    suspend fun deleteAllLinkedVillages()

    @Query("SELECT villageId as id, tenantId, name, villagecode, chiefdomId, countryId, districtId, isUserVillage, chiefdomCode, districtCode FROM LinkedVillageEntity WHERE tenantId = :tenantId")
    suspend fun getLinkedVillages(tenantId: Long): List<VillageEntity>

    @Query("SELECT v.id as villageId, v.name as villageName, COUNT(h.id) as houseHoldCount, COUNT(cd.villageId) as isCommunityProfileDetailAvailable FROM VillageEntity v LEFT JOIN Household h ON v.id = h.village_id LEFT JOIN CommunityProfile cd ON cd.villageId = v.id WHERE (:searchText = '' OR v.name LIKE '%' || :searchText || '%') GROUP BY v.id")
    fun filterCommunityProfile(searchText: String): LiveData<List<CommunityProfileDetail>>

    @Query(
        """
        SELECT COUNT(DISTINCT household_id) as householdCount,
        COUNT(CASE WHEN isActive = 1 THEN 1 END) as populationCount,
        0 as pregnantCount,
        COUNT(CASE WHEN substr(date_of_birth, 1, 10) > date('now','-1 year') AND isActive = 1 THEN 1 END ) as belowOneYearCount,
        COUNT(CASE WHEN substr(date_of_birth, 1, 10) > date('now', '-5 years')
               AND substr(date_of_birth, 1, 10) <= date('now', '-1 year') AND isActive = 1 THEN 1 END) AS belowFiveYearCount,
        COUNT(CASE WHEN gender = 'female'
               AND substr(date_of_birth, 1, 10) <= date('now','-10 years')
               AND substr(date_of_birth, 1, 10) > date('now','-49 years') AND isActive = 1 THEN 1 END) AS childBearingAgeOfWomen
        FROM HOUSEHOLDMEMBER
        WHERE villageId = :villageId""",
    )
    suspend fun getCommunityPopulationStatistics(villageId: Long): CommunityPopulationStatistics

    @Query(
        """
    SELECT h.*
    FROM HealthFacilityEntity h
    INNER JOIN VillageEntity v ON v.healthFacilityId = h.id
    WHERE v.id = :villageId
    """,
    )
    suspend fun getHealthFacilityBasedOnVillageId(villageId: Long): List<HealthFacilityEntity>

    // SubVillage methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubVillages(subVillageEntityList: List<SubVillageEntity>)

    @Query("DELETE FROM SubVillageEntity")
    suspend fun deleteAllSubVillages()

    // ShasthyaShebika methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShasthyaShebikas(shasthyaShebikaEntityList: List<ShasthyaShebikaEntity>)

    @Query("DELETE FROM ShasthyaShebikaEntity")
    suspend fun deleteAllShasthyaShebikas()

    @Query("SELECT * FROM ShasthyaShebikaEntity WHERE shasthyaKormiId = :shasthyaKormiId")
    suspend fun getShasthyaShebikaByShasthyaKormiId(shasthyaKormiId: Long): List<ShasthyaShebikaEntity>

    // ShasthyaShebikaLinkedVillage methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShasthyaShebikaLinkedVillages(linkedVillages: List<ShasthyaShebikaLinkedVillageEntity>)

    @Query("DELETE FROM ShasthyaShebikaLinkedVillageEntity")
    suspend fun deleteAllShasthyaShebikaLinkedVillages()

    @Query(
        "SELECT sv.* FROM SubVillageEntity sv " +
            "INNER JOIN ShasthyaShebikaLinkedVillageEntity sslv ON sv.id = sslv.subVillageId " +
            "WHERE sslv.shasthyaShebikaId = :shasthyaShebikaId",
    )
    suspend fun getSubVillagesByShasthyaShebikaId(shasthyaShebikaId: Long): List<SubVillageEntity>

    @Query(
        """
    SELECT DISTINCT sv.*
    FROM SubVillageEntity sv
    INNER JOIN ShasthyaShebikaLinkedVillageEntity sslv
        ON sv.id = sslv.subVillageId
    WHERE sslv.shasthyaShebikaId IN (:shasthyaShebikaIds)
    """,
    )
    suspend fun getSubVillagesByShasthyaShebikaIds(shasthyaShebikaIds: List<Long>): List<SubVillageEntity>
}
