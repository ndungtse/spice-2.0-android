package com.medtroniclabs.spice.db.local

import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.ExaminationListItems
import com.medtroniclabs.spice.data.ExaminationsComplaintItems
import com.medtroniclabs.spice.data.LabourDeliveryMetaEntity
import com.medtroniclabs.spice.data.LastCreatedAtAndPatientId
import com.medtroniclabs.spice.data.VillageInfo
import com.medtroniclabs.spice.db.dao.AboveFiveYearsDAO
import com.medtroniclabs.spice.db.dao.AssessmentDAO
import com.medtroniclabs.spice.db.dao.DiagnosisDAO
import com.medtroniclabs.spice.db.dao.ExaminationsDAO
import com.medtroniclabs.spice.db.dao.HouseholdDAO
import com.medtroniclabs.spice.db.dao.MemberClinicalDAO
import com.medtroniclabs.spice.db.dao.MemberDAO
import com.medtroniclabs.spice.db.dao.MetaDataDAO
import com.medtroniclabs.spice.db.dao.ExaminationsComplaintsDAO
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
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.dao.LabourDeliveryDAO
import javax.inject.Inject

class RoomHelperImpl @Inject constructor(
    private val householdDAO: HouseholdDAO,
    private val memberDAO: MemberDAO,
    private val assessmentDAO: AssessmentDAO,
    private val metaDataDAO: MetaDataDAO,
    private val examinationsComplaintsDAO: ExaminationsComplaintsDAO,
    private val diagnosisDAO: DiagnosisDAO,
    private val memberClinicalDAO: MemberClinicalDAO,
    private val aboveFiveYearsDAO: AboveFiveYearsDAO,
    private val examinationsDAO: ExaminationsDAO,
    private val labourDeliveryDAO: LabourDeliveryDAO
) : RoomHelper {
    override suspend fun saveHouseHoldEntry(householdEntity: HouseholdEntity): Long {
        return householdDAO.insertHouseHold(householdEntity)
    }

    override suspend fun updateHousehold(householdEntity: HouseholdEntity) {
        return householdDAO.updateHouseHold(householdEntity)
    }

    override suspend fun getLastHouseholdNo(villageId: Long): Long? {
        return householdDAO.getLastHouseholdNo(villageId)
    }

    override suspend fun getHouseHoldDetailsById(houseHoldId: Long): HouseholdEntity {
        return householdDAO.getHouseHoldDetailsById(houseHoldId)
    }

    override fun getMemberCountInHouseholdLiveData(houseHoldId: Long): LiveData<HouseholdMemberCount> {
        return householdDAO.getHouseholdMemberCountLiveData(houseHoldId)
    }

    override suspend fun registerMember(householdMemberEntity: HouseholdMemberEntity): Long {
        return memberDAO.insertMember(householdMemberEntity)
    }

    override suspend fun getAllHouseHoldMemberList(houseHoldId: Long): ArrayList<HouseholdMemberEntity> {
        return ArrayList(memberDAO.getAllHouseHoldMemberList(houseHoldId))
    }

    override suspend fun getMemberDetailsByID(memberId: Long): HouseholdMemberEntity {
        return memberDAO.getMemberDetailsById(memberId)
    }

    override suspend fun getMemberDetailsByParentId(memberId: String): List<HouseholdMemberEntity> {
        return memberDAO.getMemberDetailsByParentId(memberId)
    }

    override suspend fun getMemberCountPerHouseHold(householdId: Long): Int {
        return memberDAO.getMemberCountPerHouseHold(householdId)
    }

    override suspend fun getLastPatientId(villageId: Long): String? {
        return memberDAO.getLastPatientId(villageId)
    }

    override suspend fun getAllUnSyncedHouseHolds(): List<HouseHold> {
        return householdDAO.getAllUnSyncedHouseHolds()
    }

    override suspend fun getAllUnSyncedHouseHoldMembers(houseHoldId: Long): List<HouseHoldMember> {
        return memberDAO.getAllUnSyncedHouseHoldMembers(houseHoldId)
    }

    override suspend fun getOtherHouseholdMembers(ids: List<Long>): List<HouseHoldMember> {
        return memberDAO.getOtherHouseholdMembers(ids)
    }

    override suspend fun saveAssessment(assessmentEntity: AssessmentEntity): Long {
        return assessmentDAO.insertAssessment(assessmentEntity)
    }

    override suspend fun updateOtherAssessmentDetails(assessmentEntity: AssessmentEntity) {
        return assessmentDAO.updateOtherAssessmentDetails(assessmentEntity)
    }

    override suspend fun getLatestAssessmentForMember(memberId: Long): AssessmentEntity? {
        return assessmentDAO.getLatestAssessmentForMember(memberId)
    }

    override suspend fun insertSymptomList(symptoms: List<SignsAndSymptomsEntity>) {
        assessmentDAO.insertSymptoms(symptoms)
    }

    override suspend fun getSymptomListByType(type: String): List<SignsAndSymptomsEntity> {
        return assessmentDAO.getSymptomListByType(type)
    }

    override suspend fun saveHealthFacility(healthFacilityEntityList: HealthFacilityEntity) {
        metaDataDAO.insertHealthFacility(healthFacilityEntityList)
    }

    override suspend fun updateHeadCount(householdId: Long, newNoOfPeople: Int) {
        return householdDAO.updateHeadCount(householdId, newNoOfPeople)
    }

    override suspend fun deleteAllHealthFacility() {
        metaDataDAO.deleteAllHealthFacility()
    }

    override suspend fun saveVillage(villageEntityList: List<VillageEntity>) {
        metaDataDAO.insertVillages(villageEntityList)
    }

    override suspend fun getAllVillageEntity(): List<VillageEntity> {
        return metaDataDAO.getAllVillageName()
    }

    override suspend fun getDefaultHealthFacility(): HealthFacilityEntity? {
        return metaDataDAO.getDefaultHealthFacility()
    }

    override suspend fun getClinicalWorkflowId(
        gender: String,
        age: Int
    ): List<ClinicalWorkflowEntityWithSubmodule> {
        return metaDataDAO.getClinicalWorkflowId(gender, age)
    }

    override suspend fun deleteAllVillages() {
        metaDataDAO.deleteAllVillages()
    }

    override suspend fun deleteAllHouseholds() {
        householdDAO.deleteAllHouseholds()
    }

    override suspend fun deleteAllHouseholdMembers() {
        memberDAO.deleteAllHouseholdMembers()
    }

    override suspend fun saveMenus(menuEntity: MenuEntity) {
        metaDataDAO.insertMenus(menuEntity)
    }

    override suspend fun saveClinicalWorkflows(clinicalWorkflows: List<ClinicalWorkflowEntity>) {
        return metaDataDAO.saveClinicalWorkflows(clinicalWorkflows)
    }

    override suspend fun deleteAllClinicalWorkflow() {
        return metaDataDAO.deleteAllClinicalWorkflow()
    }

    override suspend fun saveForms(forms: List<FormEntity>) {
        return metaDataDAO.saveForms(forms)
    }

    override suspend fun deleteAllForms() {
        return metaDataDAO.deleteAllForms()
    }

    override suspend fun getAllClinicalWorkflowIds(): List<Int> {
        return metaDataDAO.getAllClinicalWorkflowIds()
    }

    override suspend fun insertSymptoms(symptomEntity: List<SignsAndSymptomsEntity>) {
        metaDataDAO.insertSymptoms(symptomEntity)
    }

    override suspend fun deleteAllSymptoms() {
        metaDataDAO.deleteAllSymptoms()
    }

    override suspend fun getChiefDomAndVillageCodeByVillageId(id: Long): VillageInfo {
        return metaDataDAO.getChiefDomAndVillageCodeByVillageId(id)
    }


    override suspend fun getFormData(formType: String): String {
        return metaDataDAO.getFormData(formType)
    }

    override suspend fun deleteAllMenus() {
        metaDataDAO.deleteAllMenus()
    }

    override suspend fun saveUserProfileDetails(userProfileEntity: UserProfileEntity) {
        metaDataDAO.insertUserProfileDetails(userProfileEntity)
    }

    override suspend fun deleteAllUserProfileDetails() {
        metaDataDAO.deleteAllUserProfileDetails()
    }

    override suspend fun getMenus(): List<MenuEntity> {
        return metaDataDAO.getMenus()
    }

    override suspend fun getUserProfile(): UserProfileEntity {
        return metaDataDAO.getUserProfile()
    }

    override suspend fun getUserVillages(): List<VillageEntity> = metaDataDAO.getVillages()

    override suspend fun getVillageByID(villageId: Long): VillageEntity =
        metaDataDAO.getVillageByID(villageId)

    override suspend fun getMenuForClinicalWorkflows(): List<ClinicalWorkflowEntity> {
        return metaDataDAO.getMenuForClinicalWorkflows()
    }

    override suspend fun deleteClinicalWorkflowConditions() {
        metaDataDAO.deleteClinicalWorkflowConditions()
    }

    override suspend fun insertClinicalWorkflowConditions(clinicalWorkflowConditions: List<ClinicalWorkflowConditionEntity>) {
        metaDataDAO.insertClinicalWorkflowConditions(clinicalWorkflowConditions)
    }

    override suspend fun getDobAndGenderById(memberId: Long): MemberDobGenderModel {
        return memberDAO.getDobAndGenderById(memberId)
    }

    override suspend fun updateFhirId(tableName: String, id: String, fhirId: String) {
        val status = OfflineSyncStatus.Success.name
        val updatedAt = System.currentTimeMillis()
        val query =
            "UPDATE $tableName SET sync_status = ?, fhir_id = ?, updated_at = ? WHERE id = ?"
        householdDAO.updateFhirId(SimpleSQLiteQuery(query, arrayOf(status, fhirId, updatedAt, id)))
    }

    override fun getFilteredHouseholdsLiveData(
        searchInput: String,
        filterByVillage: List<Long>,
        filterByStatus: String
    ): LiveData<List<HouseHoldEntityWithMemberCount>> {
        if (filterByVillage.isEmpty()) {
            return householdDAO.getHouseholdsWithFilterLiveData(searchInput, filterByStatus)
        } else {
            return householdDAO.getHouseholdsWithFilterLiveData(
                searchInput,
                filterByStatus,
                filterByVillage
            )
        }

    }

    override suspend fun getUnSyncedHouseholdCount(): Int {
        return householdDAO.getUnSyncedCount()
    }

    override suspend fun getUnSyncedHouseholdMemberCount(): Int {
        return memberDAO.getUnSyncedCount()
    }

    override suspend fun getNearestHealthFacility(): List<HealthFacilityEntity> {
        return metaDataDAO.getNearestHealthFacility()
    }

    override suspend fun getVillageIdName(): List<VillageBasicDetails> {
        return metaDataDAO.getVillageIdName()
    }

    override suspend fun getPatientVisitCountByType(
        type: String,
        patientId: String
    ): MemberClinicalEntity? {
        return memberClinicalDAO.getPatientVisitCountByType(type, patientId)
    }

    override suspend fun savePatientVisitCountByType(memberClinicalEntity: MemberClinicalEntity) {
        return memberClinicalDAO.savePatientVisitCountByType(memberClinicalEntity = memberClinicalEntity)
    }

    override suspend fun deleteExaminationsComplaints(menuType: String) {
        examinationsComplaintsDAO.deleteExaminationsComplaints(menuType)
    }

    override suspend fun insertExaminationsComplaint(symptomEntity: List<ExaminationsComplaintItems>) {
        examinationsComplaintsDAO.insertExaminationsComplaints(symptomEntity)
    }

    override suspend fun deleteDiagnosisList() {
        diagnosisDAO.deleteDiagnosisList()
    }

    override suspend fun saveDiagnosisList(diagnosisList: ArrayList<DiseaseCategoryItems>) {
        diagnosisDAO.saveDiagnosisList(diagnosisList)
    }


    override suspend fun getHouseholdIdByFhirId(fhirId: String?): Long? {
        return if (fhirId!= null) {
            householdDAO.getHouseholdIdByFhirId(fhirId)
        } else {
            null
        }
    }

    override suspend fun getHouseholdMemberIdByFhirId(fhirId: String?): Long? {
        return if (fhirId != null) {
            memberDAO.getHouseholdMemberIdByFhirId(fhirId)
        } else {
            return null
        }
    }

    override suspend fun getExaminationsComplaintByType(type:String): List<ExaminationsComplaintItems> {
        return examinationsComplaintsDAO.getExaminationsComplaintByType(type)
    }

    override suspend fun getAssessmentMemberDetails(id: Long): AssessmentMemberDetails {
        return memberDAO.getAssessmentMemberDetails(id)
    }

    override suspend fun getOtherUnSyncedAssessments(patientIds: List<String>): List<AssessmentEntity> {
        return assessmentDAO.getOtherUnSyncedAssessments(patientIds)
    }

    override suspend fun getUnSyncedAssessmentByPatientId(patientId: String): List<AssessmentEntity> {
        return assessmentDAO.getUnSyncedAssessmentByPatientId(patientId)
    }

    override suspend fun getUnSyncedAssessmentCount(): Int {
        return assessmentDAO.getUnSyncedCount()
    }

    override suspend fun updateMemberClinicalData(
        patientId: String,
        type: String,
        visitCount: Long,
        clinicalDate: String?
    ) {
        memberClinicalDAO.updateMemberClinicalData(visitCount, clinicalDate,patientId,type)
    }
    override suspend fun getSummaryDetailMetaItems(type:String): List<ExaminationsComplaintItems> {
        return aboveFiveYearsDAO.getSummaryDetailMetaItems(type)
    }

    override suspend fun deleteExaminationsComplaintsForAnc(type: String) {
        examinationsComplaintsDAO.deleteExaminationsComplaintsForAnc(type)
    }

    override fun getExaminationsComplaintsForAnc(
        category: String
    ): LiveData<List<ExaminationsComplaintItems>> {
        return examinationsComplaintsDAO.getExaminationsComplaintsForAnc(category)
    }

    override suspend fun deleteExaminationsList() {
        examinationsDAO.deleteExaminationsList()
    }
    override suspend fun saveExaminationsList(examinationList: ArrayList<ExaminationListItems>) {
        examinationsDAO.saveExaminationsList(examinationList)
    }

    override suspend fun insertLabourDelivery(symptomEntity: List<LabourDeliveryMetaEntity>) {
        labourDeliveryDAO.insertLabourDelivery(symptomEntity)
    }

    override suspend fun deleteLabourDelivery() {
        labourDeliveryDAO.deleteLabourDelivery()
    }

    override suspend fun getLabourDelivery(): List<LabourDeliveryMetaEntity> {
        return labourDeliveryDAO.getLabourDelivery()
    }
}