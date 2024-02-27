package com.medtroniclabs.spice.db.local

import androidx.room.Query
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.SymptomEntity
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount

interface RoomHelper {
    suspend fun saveHouseHoldEntry(householdEntity: HouseholdEntity): Long
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
    suspend fun saveClinicalWorkflow(clinicalWorkflowEntity: ClinicalWorkflowEntity)
    suspend fun deleteAllClinicalWorkflow()
    suspend fun getAllClinicalWorkflowIds(): List<Int>
    suspend fun saveForm(form: FormEntity)
    suspend fun deleteAllForms()
    suspend fun getFormData(
        formType: String
    ): String
    suspend fun getFomDataForWorkFlow(formType: String, workflowName: String): String
    suspend fun insertSymptoms(symptomEntity: SignsAndSymptomsEntity)
    suspend fun deleteAllSymptoms()
}