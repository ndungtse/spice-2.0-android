package com.medtroniclabs.spice.db.local

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.ExaminationListItems
import com.medtroniclabs.spice.data.ExaminationsComplaintItems
import com.medtroniclabs.spice.data.VillageInfo
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowConditionEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntityWithSubmodule
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.db.response.HouseholdMemberCount
import com.medtroniclabs.spice.db.response.VillageBasicDetails
import com.medtroniclabs.spice.model.MemberDobGenderModel
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails

interface RoomHelper {
    suspend fun saveHouseHoldEntry(householdEntity: HouseholdEntity): Long
    suspend fun updateHousehold(householdEntity: HouseholdEntity)
    suspend fun getLastHouseholdNo(villageId: Long): Long?
    suspend fun getHouseHoldDetailsById(houseHoldId: Long): HouseholdEntity
    suspend fun registerMember(householdMemberEntity: HouseholdMemberEntity): Long
    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): ArrayList<HouseholdMemberEntity>
    suspend fun getMemberDetailsByID(memberId: Long): HouseholdMemberEntity
    suspend fun getMemberDetailsByParentId(memberId: Long): List<HouseholdMemberEntity>
    suspend fun saveAssessment(assessmentEntity: AssessmentEntity): Long
    suspend fun updateOtherAssessmentDetails(assessmentEntity: AssessmentEntity)
    suspend fun getLatestAssessmentForMember(memberId: Long): AssessmentEntity?
    suspend fun insertSymptomList(symptoms: List<SignsAndSymptomsEntity>)
    suspend fun getSymptomListByType(type: String): List<SignsAndSymptomsEntity>
    suspend fun updateHeadCount(householdId: Long, newNoOfPeople: Int)
    suspend fun getMemberCountPerHouseHold(householdId: Long): Int
    suspend fun saveHealthFacility(healthFacilityEntityList: HealthFacilityEntity)
    suspend fun deleteAllHealthFacility()
    suspend fun saveVillage(villageEntityList: List<VillageEntity>)
    suspend fun getAllVillageEntity(): List<VillageEntity>
    suspend fun deleteAllVillages()
    suspend fun deleteAllHouseholds()
    suspend fun deleteAllHouseholdMembers()
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
    suspend fun getMenuForClinicalWorkflows(): List<ClinicalWorkflowEntity>
    suspend fun deleteClinicalWorkflowConditions()
    suspend fun insertClinicalWorkflowConditions(clinicalWorkflowConditions: List<ClinicalWorkflowConditionEntity>)
    suspend fun getUserVillages(): List<VillageEntity>
    suspend fun getVillageByID(villageId: Long): VillageEntity
    suspend fun getChiefDomAndVillageCodeByVillageId(id: Long): VillageInfo
    suspend fun getLastPatientId(villageId: Long): String?
    fun getMemberCountInHouseholdLiveData(houseHoldId: Long): LiveData<HouseholdMemberCount>
    suspend fun getClinicalWorkflowId(
        gender: String,
        age: Int
    ): List<ClinicalWorkflowEntityWithSubmodule>

    suspend fun getDobAndGenderById(memberId: Long): MemberDobGenderModel
    suspend fun getAllUnSyncedHouseHolds(): List<HouseHold>

    suspend fun getAllUnSyncedHouseHoldMembers(houseHoldId: Long): List<HouseHoldMember>

    suspend fun getOtherHouseholdMembers(ids: List<Long>): List<HouseHoldMember>
    suspend fun updateFhirId(tableName: String, id: String, fhirId: String)
    fun getFilteredHouseholdsLiveData(
        searchInput: String,
        filterByVillage: List<Long>,
        filterByStatus: String
    ): LiveData<List<HouseHoldEntityWithMemberCount>>

    suspend fun getNearestHealthFacility(): List<HealthFacilityEntity>
    suspend fun getUnSyncedHouseholdCount(): Int
    suspend fun getUnSyncedHouseholdMemberCount(): Int
    suspend fun getVillageIdName(): List<VillageBasicDetails>
    suspend fun getPatientVisitCountByType(type: String, patientId: String): MemberClinicalEntity?
    suspend fun savePatientVisitCountByType(memberClinicalEntity: MemberClinicalEntity)
    suspend fun deleteExaminationsComplaints(menuType: String)
    suspend fun insertExaminationsComplaint(symptomEntity: List<ExaminationsComplaintItems>)
    suspend fun deleteDiagnosisList()
    suspend fun saveDiagnosisList(diagnosisList: ArrayList<DiseaseCategoryItems>)
    suspend fun getHouseholdIdByFhirId(fhirId: String?): Long?
    suspend fun getHouseholdMemberIdByFhirId(fhirId: String?): Long?
    suspend fun getExaminationsComplaintByType(type: String): List<ExaminationsComplaintItems>
    suspend fun getAssessmentMemberDetails(id: Long): AssessmentMemberDetails
    suspend fun getUnSyncedAssessmentByPatientId(patientId: String): List<AssessmentEntity>
    suspend fun getOtherUnSyncedAssessments(patientIds: List<String>): List<AssessmentEntity>
    suspend fun getUnSyncedAssessmentCount(): Int

    suspend fun updateMemberClinicalData(
        patientId: String,
        type: String,
        visitCount: Long,
        clinicalDate: String?
    )
    suspend fun getSummaryDetailMetaItems(type: String): List <ExaminationsComplaintItems>
    suspend fun deleteExaminationsComplaintsForAnc(type: String)

    fun getExaminationsComplaintsForAnc(
        category: String
    ): LiveData<List<ExaminationsComplaintItems>>
    suspend fun deleteExaminationsList()
    suspend fun saveExaminationsList(diagnosisList: ArrayList<ExaminationListItems>)
}