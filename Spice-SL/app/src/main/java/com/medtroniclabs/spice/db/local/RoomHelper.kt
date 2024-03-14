package com.medtroniclabs.spice.db.local

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.data.LastCreatedAtAndPatientId
import com.medtroniclabs.spice.data.VillageInfo
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowConditionEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.model.MemberDobGenderModel
import com.medtroniclabs.spice.db.response.HouseholdMemberCount
import com.medtroniclabs.spice.offlinesync.model.HouseHold
import com.medtroniclabs.spice.offlinesync.model.HouseHoldMember

interface RoomHelper {
    suspend fun saveHouseHoldEntry(householdEntity: HouseholdEntity): Long
    suspend fun updateHousehold(householdEntity: HouseholdEntity)
    suspend fun getHouseHoldList(): ArrayList<HouseHoldEntityWithMemberCount>
    suspend fun getLastHouseholdNo(villageId: Long): Long?
    suspend fun searchByHouseholdNameOrNo(searchTerm: String): ArrayList<HouseHoldEntityWithMemberCount>
    suspend fun getHouseHoldDetailsById(houseHoldId: Long): HouseholdEntity
    suspend fun registerMember(householdMemberEntity: HouseholdMemberEntity): Long
    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): ArrayList<HouseholdMemberEntity>
    suspend fun getMemberDetailsByID(memberId: Long): HouseholdMemberEntity
    suspend fun saveAssessment(assessmentEntity: AssessmentEntity): Long
    suspend fun updateOtherAssessmentDetails(assessmentEntity: AssessmentEntity)
    suspend fun getLatestAssessmentForMember(memberId: Long): AssessmentEntity?
    suspend fun insertSymptomList(symptoms: List<SignsAndSymptomsEntity>)
    suspend fun getSymptomListByType(type: String): List<SignsAndSymptomsEntity>
    suspend fun updateHeadCount(householdId: Long, newNoOfPeople: Int)
    suspend fun getMemberCountPerHouseHold(householdId: Long): Int
    suspend fun saveHealthFacility(healthFacilityEntityList:HealthFacilityEntity)
    suspend fun deleteAllHealthFacility()
    suspend fun saveVillage(villageEntityList: List<VillageEntity>)
    suspend fun getAllVillageName(): List<VillageEntity>
    suspend fun deleteAllVillages()
    suspend fun saveMenus(menuEntity: MenuEntity)
    suspend fun deleteAllMenus()
    suspend fun saveUserProfileDetails(userProfileEntity: UserProfileEntity)
    suspend fun deleteAllUserProfileDetails()
    suspend fun getMenus(): List<MenuEntity>
    suspend fun getUserProfile(): UserProfileEntity
    suspend fun getDefaultHealthFacility(): HealthFacilityEntity?
    suspend fun saveClinicalWorkflows(clinicalWorkflows: List<ClinicalWorkflowEntity>)
    suspend fun deleteAllClinicalWorkflow()
    suspend fun getAllClinicalWorkflowIds(): List<Int>
    suspend fun saveForms(forms: List<FormEntity>)
    suspend fun deleteAllForms()
    suspend fun getFormData(
        formType: String
    ): String
    suspend fun insertSymptoms(symptomEntity: List<SignsAndSymptomsEntity>)
    suspend fun deleteAllSymptoms()
    suspend fun getMenuForClinicalWorkflows() :List<ClinicalWorkflowEntity>
    suspend fun deleteClinicalWorkflowConditions()
    suspend fun insertClinicalWorkflowConditions(clinicalWorkflowConditions: List<ClinicalWorkflowConditionEntity>)
    suspend fun getUserVillages(): List<VillageEntity>
    suspend fun getVillageByID(villageId: Long): VillageEntity
    suspend fun getChiefDomAndVillageCodeByVillageId(id: Long): VillageInfo
    suspend fun getLastPatientId(): LastCreatedAtAndPatientId
    fun getMemberCountInHouseholdLiveData(houseHoldId: Long): LiveData<HouseholdMemberCount>
    suspend fun getClinicalWorkflowId(gender: String, age: Int): List<ClinicalWorkflowEntity>
    suspend fun getDobAndGenderById(memberId: Long): MemberDobGenderModel
    suspend fun getAllUnSyncedHouseHolds(): List<HouseHold>

    suspend fun getAllUnSyncedHouseHoldMembers(houseHoldId: Long): List<HouseHoldMember>

    suspend fun getOtherHouseholdMembers(ids:List<Long>): List<HouseHoldMember>
    suspend fun updateFhirId(tableName: String, id: String, fhirId: String)
}