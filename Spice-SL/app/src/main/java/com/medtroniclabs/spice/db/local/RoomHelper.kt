package com.medtroniclabs.spice.db.local

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.ExaminationListItems
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.data.LabourDeliveryMetaEntity
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.VillageInfo
import com.medtroniclabs.spice.data.model.HouseholdCardDetail
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.data.offlinesync.model.RequestFollowUp
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowConditionEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntityWithSubmodule
import com.medtroniclabs.spice.db.entity.FollowUp
import com.medtroniclabs.spice.db.entity.FollowUpCall
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
import com.medtroniclabs.spice.model.assessment.AssessmentDetails
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.model.followup.FollowUpFilter

interface RoomHelper {
    suspend fun saveHouseHoldEntry(householdEntity: HouseholdEntity): Long
    suspend fun updateHousehold(householdEntity: HouseholdEntity)
    suspend fun getLastHouseholdNo(villageId: Long): Long?
    suspend fun getHouseHoldDetailsById(houseHoldId: Long): HouseholdEntity
    suspend fun registerMember(householdMemberEntity: HouseholdMemberEntity): Long
    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): ArrayList<HouseholdMemberEntity>
    suspend fun getMemberDetailsByID(memberId: Long): HouseholdMemberEntity
    suspend fun getMemberDetailsByParentId(memberId: String): List<HouseholdMemberEntity>
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
    suspend fun getLastPatientId(patientIdStarts: String): String?
    fun getMemberCountInHouseholdLiveData(houseHoldId: Long): LiveData<HouseholdMemberCount>
    suspend fun getClinicalWorkflowId(
        gender: String,
        age: Int
    ): List<ClinicalWorkflowEntityWithSubmodule>

    suspend fun getDobAndGenderById(memberId: Long): MemberDobGenderModel
    suspend fun getAllUnSyncedHouseHolds(): List<HouseHold>

    suspend fun getAllUnSyncedHouseHoldMembers(houseHoldId: Long): List<HouseHoldMember>

    suspend fun getOtherHouseholdMembers(): List<HouseHoldMember>
    suspend fun updateFhirId(tableName: String, id: String, fhirId: String?, status: String)
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
    suspend fun insertExaminationsComplaint(symptomEntity: List<MedicalReviewMetaItems>)
    suspend fun deleteDiagnosisList(diagnosisType: String)
    suspend fun saveDiagnosisList(diagnosisList: ArrayList<DiseaseCategoryItems>)
    suspend fun getHouseholdIdByFhirId(fhirId: String?): Long?
    suspend fun getHouseholdMemberIdByFhirId(fhirId: String?): Long?
    suspend fun getExaminationsComplaintByType(type: String): List<MedicalReviewMetaItems>
    suspend fun getAssessmentMemberDetails(id: Long): AssessmentMemberDetails
    suspend fun getUnSyncedAssessmentByPatientId(patientId: String): List<AssessmentDetails>
    suspend fun getOtherUnSyncedAssessments(): List<AssessmentDetails>
    suspend fun getUnSyncedAssessmentCount(): Int

    suspend fun deleteAllAssessments()
    suspend fun updateMemberClinicalData(
        patientId: String,
        type: String,
        visitCount: Long,
        clinicalDate: String?
    )

    suspend fun getSummaryDetailMetaItems(type: String): List<MedicalReviewMetaItems>
    suspend fun deleteExaminationsComplaintsForAnc(type: String)

    fun getExaminationsComplaintsForAnc(
        category: String,
        type: String
    ): LiveData<List<MedicalReviewMetaItems>>

    suspend fun deleteExaminationsList(menuType: String)
    suspend fun saveExaminationsList(diagnosisList: ArrayList<ExaminationListItems>)

    suspend fun insertLabourDelivery(symptomEntity: List<LabourDeliveryMetaEntity>)
    suspend fun deleteLabourDelivery()
    suspend fun getLabourDelivery(): List<LabourDeliveryMetaEntity>
    suspend fun getDiagnosisList(diagnosisType: String): List<DiseaseCategoryItems>

    suspend fun getExaminationQuestionsByWorkFlow(workFlowType: String): ExaminationListItems

    suspend fun insertFollowUp(followUp: FollowUp): Long
    suspend fun insertFollowUps(list: List<FollowUp>)

    suspend fun deleteAllFollowUps()

    fun getFollowUpPatientListLiveData(
        type: String,
        search: String? = null,
        villageIds: List<Long> = listOf(),
        fromDate: String = "",
        toDate: String = ""
    ): LiveData<List<FollowUpPatientModel>>

    suspend fun getAllVillageIds(): List<Long>

    suspend fun deleteAllMemberClinical()
    suspend fun getPatientIdByFhirId(fhirId: String): String?

    suspend fun insertClinicalInfos(list: List<MemberClinicalEntity>)

    suspend fun addCallHistory(oldFollowUp: FollowUp, history: FollowUpCall, newFollowUp: FollowUp? = null)

    suspend fun deleteAllFollowUpCalls()

    suspend fun getFollowUpById(id: Long): FollowUp

    suspend fun getAllFollowUpRequests(): List<FollowUp>

    suspend fun getAllFollowUpCalls(id: Long): List<FollowUpCall>

    suspend fun getUnSyncedFollowUpCount(): Int

    fun getExaminationsComplaintByTypeLiveData(category: String): LiveData<List<MedicalReviewMetaItems>>

    fun getHouseholdCardDetailLiveData(id: Long): LiveData<HouseholdCardDetail>

    fun getAllHouseHoldMembersLiveData(hhId: Long) : LiveData<List<HouseholdMemberEntity>>

    suspend fun updateOtherDuplicateTickets(id: Long, followUp: FollowUp)

    suspend fun updateOnTreatmentStatus(id: Long, followUp: FollowUp, updatedAt: Long)

    suspend fun changeHouseholdStatus(idList: List<String>, status: String)

    suspend fun changeHouseholdMemberStatus(idList: List<String>, status: String)

    suspend fun insertOrUpdateHHFromBE(entity: HouseholdEntity): Long

    suspend fun insertOrUpdateHHMFromBE(entity: HouseholdMemberEntity): Long
}