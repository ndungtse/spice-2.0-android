package com.medtroniclabs.spice.db.local

import androidx.lifecycle.LiveData
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.CulturesEntity
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.DosageFrequency
import com.medtroniclabs.spice.data.ExaminationListItems
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.data.LabourDeliveryMetaEntity
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.ProgramEntity
import com.medtroniclabs.spice.data.ShortageReasonEntity
import com.medtroniclabs.spice.data.UnitMetricEntity
import com.medtroniclabs.spice.data.community.CommunityPopulationStatistics
import com.medtroniclabs.spice.data.community.CommunityProfileDetail
import com.medtroniclabs.spice.data.model.HouseholdCardDetail
import com.medtroniclabs.spice.data.offlinesync.model.HHSignatureDetail
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.HouseHoldMember
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberCallRegisterDto
import com.medtroniclabs.spice.data.offlinesync.model.UnAssignedHouseholdMemberDetail
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.dao.AboveFiveYearsDAO
import com.medtroniclabs.spice.db.dao.AssessmentDAO
import com.medtroniclabs.spice.db.dao.CallHistoryDao
import com.medtroniclabs.spice.db.dao.CommunityDetailsDAO
import com.medtroniclabs.spice.db.dao.ConsentFormDao
import com.medtroniclabs.spice.db.dao.DiagnosisDAO
import com.medtroniclabs.spice.db.dao.ExaminationsComplaintsDAO
import com.medtroniclabs.spice.db.dao.ExaminationsDAO
import com.medtroniclabs.spice.db.dao.FollowUpCallsDao
import com.medtroniclabs.spice.db.dao.FollowUpDao
import com.medtroniclabs.spice.db.dao.FrequencyDAO
import com.medtroniclabs.spice.db.dao.HouseholdDAO
import com.medtroniclabs.spice.db.dao.LabourDeliveryDAO
import com.medtroniclabs.spice.db.dao.LinkHouseholdMemberDao
import com.medtroniclabs.spice.db.dao.MemberDAO
import com.medtroniclabs.spice.db.dao.MetaDataDAO
import com.medtroniclabs.spice.db.dao.NCDFollowUpDao
import com.medtroniclabs.spice.db.dao.NcdMedicalReviewDao
import com.medtroniclabs.spice.db.dao.PregnancyDetailDao
import com.medtroniclabs.spice.db.dao.RiskFactorDAO
import com.medtroniclabs.spice.db.dao.ScreeningDAO
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.CallHistory
import com.medtroniclabs.spice.db.entity.ChiefDomEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowConditionEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.CommunityProfile
import com.medtroniclabs.spice.db.entity.ConsentEntity
import com.medtroniclabs.spice.db.entity.ConsentForm
import com.medtroniclabs.spice.db.entity.DistrictEntity
import com.medtroniclabs.spice.db.entity.DosageDurationEntity
import com.medtroniclabs.spice.db.entity.FollowUp
import com.medtroniclabs.spice.db.entity.FollowUpCall
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.FrequencyEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.LifestyleEntity
import com.medtroniclabs.spice.db.entity.LinkHouseholdMember
import com.medtroniclabs.spice.db.entity.LinkedVillageEntity
import com.medtroniclabs.spice.db.entity.MedicalComplianceEntity
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.db.entity.MentalHealthEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.NCDAssessmentClinicalWorkflow
import com.medtroniclabs.spice.db.entity.NCDCallDetails
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.db.entity.NCDFollowUp
import com.medtroniclabs.spice.db.entity.NCDMedicalReviewMetaEntity
import com.medtroniclabs.spice.db.entity.NCDPatientDetailsEntity
import com.medtroniclabs.spice.db.entity.PregnancyDetail
import com.medtroniclabs.spice.db.entity.RiskFactorEntity
import com.medtroniclabs.spice.db.entity.ScreeningEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.TreatmentPlanEntity
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.db.response.HouseholdMemberCount
import com.medtroniclabs.spice.model.MemberDobGenderModel
import com.medtroniclabs.spice.model.assessment.AssessmentDetails
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.ui.assessment.AssessmentNCDEntity
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.boarding.MenuTypeEnums
import com.medtroniclabs.spice.ui.followup.FollowUpDefinedParams
import javax.inject.Inject

class RoomHelperImpl @Inject constructor(
    private val householdDAO: HouseholdDAO,
    private val memberDAO: MemberDAO,
    private val assessmentDAO: AssessmentDAO,
    private val metaDataDAO: MetaDataDAO,
    private val examinationsComplaintsDAO: ExaminationsComplaintsDAO,
    private val diagnosisDAO: DiagnosisDAO,
    private val aboveFiveYearsDAO: AboveFiveYearsDAO,
    private val examinationsDAO: ExaminationsDAO,
    private val labourDeliveryDAO: LabourDeliveryDAO,
    private val followUpDao: FollowUpDao,
    private val followUpCallsDao: FollowUpCallsDao,
    private val pregnancyDetailDao: PregnancyDetailDao,
    private val frequencyDAO: FrequencyDAO,
    private val consentFormDao: ConsentFormDao,
    private val linkHouseholdMemberDao: LinkHouseholdMemberDao,
    private val callHistoryDao: CallHistoryDao,
    private val screeningDAO: ScreeningDAO,
    private val riskFactorDAO: RiskFactorDAO,
    private val ncdMedicalReviewDao: NcdMedicalReviewDao,
    private val ncdFollowUpDao: NCDFollowUpDao,
    private val communityDAO: CommunityDetailsDAO
) : RoomHelper {
    override suspend fun saveHouseHoldEntry(householdEntity: HouseholdEntity): Long {
        return householdDAO.insertHouseHold(householdEntity)
    }

    override suspend fun updateHousehold(householdEntity: HouseholdEntity) {
        return householdDAO.updateHouseHold(householdEntity)
    }

    override suspend fun updateHeadPhoneNumber(id: Long, phoneNumber: String, phoneNumberCategory: String){
        return householdDAO.updateHeadPhoneNumber(id,phoneNumber, phoneNumberCategory)
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

    override suspend fun getLastPatientId(patientIdStarts: String): String? {
        return memberDAO.getLastPatientId(patientIdStarts)
    }

    override suspend fun getAllUnSyncedHouseHolds(): List<HouseHold> {
        return householdDAO.getAllUnSyncedHouseHolds()
    }

    override suspend fun getAllUnSyncedHouseHoldMembers(houseHoldId: Long): List<HouseHoldMember> {
        return memberDAO.getAllUnSyncedHouseHoldMembers(houseHoldId)
    }

    override suspend fun getOtherHouseholdMembers(memberIds: List<String>): List<HouseHoldMember> {
        return memberDAO.getOtherHouseholdMembers(memberIds)
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
        return metaDataDAO.getVillages()
    }

    override suspend fun getAllLinkedVillageEntity(): List<VillageEntity> {
        return metaDataDAO.getLinkedVillages(SecuredPreference.getTenantId())
    }

    override suspend fun getVillagesByChiefDom(chiefdomId: Long): List<VillageEntity> {
        return metaDataDAO.getVillagesByChiefDom(chiefdomId)
    }

    override suspend fun getDefaultHealthFacility(): HealthFacilityEntity? {
        return metaDataDAO.getDefaultHealthFacility()
    }

    override suspend fun getClinicalWorkflowId(
        gender: String,
        age: Int
    ): List<NCDAssessmentClinicalWorkflow> {
        return metaDataDAO.getClinicalWorkflowId(gender, age, MenuTypeEnums.assessment.name)
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

    override suspend fun updateFhirId(
        tableName: String,
        id: String,
        fhirId: String?,
        status: String
    ) {
        val updatedAt = System.currentTimeMillis()
        val query =
            "UPDATE $tableName SET fhir_id = ?, updated_at = ?, sync_status = CASE WHEN sync_status = 'InProgress' THEN ? WHEN sync_status = 'NetworkError' THEN ? ELSE sync_status END WHERE id = ?"
        householdDAO.updateFhirId(SimpleSQLiteQuery(query, arrayOf(fhirId, updatedAt, status, status, id)))
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

    override suspend fun getPatientVisitCountByType(
        type: String,
        hhmLocalId: Long,
    ): MemberClinicalEntity? {
        return when (type) {
            RMNCH.ANC -> pregnancyDetailDao.getAncDetail(hhmLocalId)
            RMNCH.PNC -> pregnancyDetailDao.getPncDetail(hhmLocalId)
            else -> pregnancyDetailDao.getChildhoodVisitDetail(hhmLocalId)
        }
    }

    override suspend fun savePatientVisitCountByType(memberClinicalEntity: MemberClinicalEntity) {
        //  return memberClinicalDAO.savePatientVisitCountByType(memberClinicalEntity = memberClinicalEntity)
    }

    override suspend fun deleteExaminationsComplaints(menuType: String) {
        examinationsComplaintsDAO.deleteExaminationsComplaints(menuType)
    }

    override suspend fun insertExaminationsComplaint(symptomEntity: List<MedicalReviewMetaItems>) {
        examinationsComplaintsDAO.insertExaminationsComplaints(symptomEntity)
    }

    override suspend fun deleteDiagnosisList(diagnosisType: String) {
        diagnosisDAO.deleteDiagnosisList(diagnosisType)
    }

    override suspend fun saveDiagnosisList(diagnosisList: ArrayList<DiseaseCategoryItems>) {
        diagnosisDAO.saveDiagnosisList(diagnosisList)
    }


    override suspend fun getHouseholdIdByFhirId(fhirId: String?): Long? {
        return if (fhirId != null) {
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

    override suspend fun getExaminationsComplaintByType(type: String): List<MedicalReviewMetaItems> {
        return examinationsComplaintsDAO.getExaminationsComplaintByType(type)
    }

    override suspend fun getAssessmentMemberDetails(id: Long): AssessmentMemberDetails {
        return memberDAO.getAssessmentMemberDetails(id)
    }

    override suspend fun getOtherUnSyncedAssessments(addedAssessmentIds: List<String>): List<AssessmentDetails> {
        return assessmentDAO.getOtherUnSyncedAssessments(addedAssessmentIds)
    }

    override suspend fun getUnSyncedAssessmentByHHMId(hhmId: Long): List<AssessmentDetails> {
        return assessmentDAO.getUnSyncedAssessmentByHHMId(hhmId)
    }

    override suspend fun getUnSyncedAssessmentCount(): Int {
        return assessmentDAO.getUnSyncedCount()
    }

    override suspend fun updatePregnancyAncDetail(
        hhmLocalId: Long,
        visitCount: Long,
        clinicalDate: String?
    ) {
        pregnancyDetailDao.updatePregnancyAnc(visitCount, clinicalDate, hhmLocalId)
    }

    override suspend fun getSummaryDetailMetaItems(type: String): List<MedicalReviewMetaItems> {
        return aboveFiveYearsDAO.getSummaryDetailMetaItems(type)
    }

    override suspend fun deleteExaminationsComplaintsForAnc(type: String) {
        examinationsComplaintsDAO.deleteExaminationsComplaintsForAnc(type)
    }

    override fun getExaminationsComplaintsForAnc(
        category: String,
        type: String
    ): LiveData<List<MedicalReviewMetaItems>> {
        return examinationsComplaintsDAO.getExaminationsComplaintsForAnc(category, type)
    }

    override suspend fun deleteExaminationsList(menuType: String) {
        examinationsDAO.deleteExaminationsList(menuType)
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

    override suspend fun getDiagnosisList(diagnosisType: String): List<DiseaseCategoryItems> {
        return diagnosisDAO.getDiagnosisList(diagnosisType)
    }

    override suspend fun insertFollowUp(followUp: FollowUp): Long {
        return followUpDao.insertFollowUp(followUp)
    }

    override suspend fun deleteAllFollowUps() {
        followUpDao.deleteAllFollowUps()
    }

    override suspend fun deleteAllUnAssignedMember() {
        linkHouseholdMemberDao.deleteAllLinkHouseholdMember()
    }

    override suspend fun deleteAllCallHistory() {
        callHistoryDao.deleteAllCallHistory()
    }

    override suspend fun deleteAllCommunityProfiles() {
        communityDAO.deleteAllCommunityProfiles()
    }

    override fun getFollowUpPatientListLiveData(
        type: String,
        search: String?,
        villageIds: List<Long>,
        fromDate: String,
        toDate: String
    ): LiveData<List<FollowUpPatientModel>> {
        if (type == FollowUpDefinedParams.FU_TYPE_REFERRED) {
            return followUpDao.getReferredFollowUpPatientListLiveData(
                type = type,
                search = search,
                villageIds = villageIds,
                fromDate = fromDate,
                toDate = toDate
            )
        } else {
            return followUpDao.getOtherFollowUpPatientListLiveData(
                type = type,
                search = search,
                villageIds = villageIds,
                fromDate = fromDate,
                toDate = toDate
            )
        }
    }

    override suspend fun getAllVillageIds(): List<Long> {
        return metaDataDAO.getVillageIds()
    }

    override suspend fun getExaminationQuestionsByWorkFlow(workFlowType: String): ExaminationListItems {
        return examinationsDAO.getExaminationsByType(workFlowType)
    }

    override suspend fun getPatientIdByFhirId(fhirId: String): String? {
        return memberDAO.getPatientIdByFhirId(fhirId)
    }

    override suspend fun deleteAllPregnancyDetails() {
        pregnancyDetailDao.deleteAllPregnancyDetails()
    }

    override suspend fun insertUpdatePregnancyDetailFromBE(pregnancyDetail: PregnancyDetail) {
        pregnancyDetailDao.insertOrUpdateFromBE(pregnancyDetail)
    }

    override suspend fun addCallHistory(
        oldFollowUp: FollowUp,
        history: FollowUpCall,
        newFollowUp: FollowUp?
    ) {
        followUpCallsDao.insertFollowUpCall(history)
        followUpDao.insertFollowUp(oldFollowUp)
        newFollowUp?.let {
            followUpDao.insertFollowUp(it)
        }
    }


    override suspend fun getAllFollowUpRequests(): List<FollowUp> {
        return followUpDao.getAllFollowUps()
    }

    override suspend fun getAllFollowUpCalls(id: Long): List<FollowUpCall> {
        return followUpCallsDao.getAllFollowUpCalls(id)
    }

    override suspend fun getFollowUpById(id: Long): FollowUp {
        return followUpDao.getFollowUpDetailsById(id)
    }

    override suspend fun deleteAllFollowUpCalls() {
        return followUpCallsDao.deleteAllFollowUpCalls()
    }

    override suspend fun getUnSyncedFollowUpCount(): Int {
        return followUpDao.getUnSyncedCount()
    }

    override suspend fun getUnSyncedCommunityProfileCount(): Int {
        return communityDAO.getUnSyncedCount()
    }

    override suspend fun deleteAllAssessments() {
        return assessmentDAO.deleteAllAssessments()
    }

    override fun getExaminationsComplaintByTypeLiveData(category: String): LiveData<List<MedicalReviewMetaItems>> {
        return examinationsComplaintsDAO.getExaminationsComplaintByTypeLiveData(category)
    }

    override fun getHouseholdCardDetailLiveData(id: Long): LiveData<HouseholdCardDetail> {
        return householdDAO.getHouseholdCardDetailLiveData(id)
    }

    override fun getAllHouseHoldMembersLiveData(hhId: Long): LiveData<List<HouseholdMemberEntity>> {
        return memberDAO.getAllHouseHoldMembersLiveData(hhId)
    }

    override fun getAliveHouseHoldMembersLiveData(hhId: Long): List<HouseholdMemberEntity> {
      return memberDAO.getAliveHouseHoldMembers(hhId,true)
    }

    override suspend fun updateOtherDuplicateTickets(
        id: Long,
        followUp: FollowUp
    ) {
        followUpDao.updateOtherDuplicateTickets(
            id,
            followUp.memberId,
            followUp.type,
            followUp.encounterType,
            followUp.reason
        )
    }

    override suspend fun updateDuplicateTicketsAsCompleted(id: Long, followUp: FollowUp) {
        if (followUp.type == FollowUpDefinedParams.FU_TYPE_HH_VISIT) {
            followUpDao.updateHHVisitTicketsOnRecovered(id,followUp.memberId, followUp.type, followUp.encounterType, followUp.reason)
        } else {
            val types = listOf(FollowUpDefinedParams.FU_TYPE_REFERRED, FollowUpDefinedParams.FU_TYPE_MEDICAL_REVIEW)
            if (followUp.encounterType == FollowUpDefinedParams.FU_ENCOUNTER_TYPE_RMNCH) {
                followUpDao.closeTicketsForRMNCH(id, followUp.memberId, types)
            } else {
                followUpDao.closeTicketsForNonRMNCH(id,followUp.memberId, followUp.type, types)
            }
        }
    }

    override suspend fun updateOnTreatmentStatus(
        id: Long,
        followUp: FollowUp,
        updateAt: Long
    ) {
        followUpDao.updateOnTreatmentStatus(
            id,
            followUp.memberId,
            followUp.type,
            updateAt,
            followUp.encounterType,
            followUp.reason
        )
    }

    override suspend fun insertOrUpdateHHFromBE(entity: HouseholdEntity): Long {
        return householdDAO.insertOrUpdateFromBE(entity)
    }

    override suspend fun insertOrUpdateHHMFromBE(entity: HouseholdMemberEntity): Long {
        return memberDAO.insertOrUpdateFromBE(entity)
    }

    override suspend fun changeHouseholdStatus(idList: List<String>,  syncStatus: String) {
        householdDAO.updateInProgress(idList, syncStatus)
    }

    override suspend fun changeHouseholdMemberStatus(idList: List<String>, syncStatus: String) {
        memberDAO.updateInProgress(idList, syncStatus)
    }

    override suspend fun changeAssessmentStatus(idList: List<String>, syncStatus: String) {
        assessmentDAO.updateInProgress(idList, syncStatus)
    }

    override suspend fun changeFollowUpStatus(idList: List<Long>, syncStatus: String) {
        followUpDao.updateInProgress(idList, syncStatus)
    }

    override suspend fun getPregnancyDetailByPatientId(hhmLocalId: Long): PregnancyDetail? {
        return pregnancyDetailDao.getPregnancyDetailByPatientId(hhmLocalId)
    }

    override suspend fun savePregnancyDetail(detail: PregnancyDetail): Long {
        return pregnancyDetailDao.savePregnancyDetail(detail)
    }

    override suspend fun deleteAllFrequencyList() {
        return frequencyDAO.deleteAllVillages()
    }

    override suspend fun saveFrequencyList(frequencyList: List<FrequencyEntity>): List<Long> {
        return frequencyDAO.insertFrequencyList(frequencyList)
    }

    override suspend fun getFrequencyList(): List<FrequencyEntity> {
        return frequencyDAO.getFrequencyList()
    }

    override fun getExaminationsComplaintsForPnc(
        category: String,
        type: String
    ): LiveData<List<MedicalReviewMetaItems>> {
        return examinationsComplaintsDAO.getExaminationsComplaintsForPnc(category, type)
    }

    override suspend fun updateOtherFollowUpForWrongNumber(id: Long, fhirId: String) {
        followUpDao.updateOtherFollowUpForWrongNumber(id, fhirId)
    }

    override suspend fun insertOrUpdateFollowUp(entity: FollowUp) {
        followUpDao.insertOrUpdateFromBE(entity)
    }

    override suspend fun deleteCompletedFollowUp() {
        followUpDao.deleteCompletedFollowUp()
    }

    override suspend fun getUserHealthFacility(isUserSite: Boolean): ArrayList<HealthFacilityEntity> {
        return ArrayList(metaDataDAO.getUserHealthFacility(isUserSite))
    }

    override suspend fun updateMemberDeceasedStatus(id: Long, status: Boolean) {
        memberDAO.updateMemberDeceasedStatus(id, status, OfflineSyncStatus.NotSynced)
    }

    override suspend fun saveForm(forms: FormEntity) {
        return metaDataDAO.saveForm(forms)
    }

    override suspend fun saveConsent(consentEntity: ConsentEntity) {
        return metaDataDAO.insertConsent(consentEntity)
    }

    override fun getConsent(formType: String): LiveData<String> {
        return metaDataDAO.getConsent(formType)
    }

    override suspend fun deleteConsent() {
        return metaDataDAO.deleteConsent()
    }

    override suspend fun saveModelQuestions(mentalHealthEntity: List<MentalHealthEntity>) {
        return metaDataDAO.insertModelQuestions(mentalHealthEntity)
    }

    override suspend fun getModelQuestions(formType: String): MentalHealthEntity {
        return metaDataDAO.getModelQuestions(formType)
    }

    override suspend fun deleteModelQuestions() {
        return metaDataDAO.deleteModelQuestions()
    }

    override suspend fun changeFollowUpCallStatus(idList: List<Long>) {
        followUpCallsDao.updateSyncSuccess(idList)
    }

    override suspend fun updateNeonatePatientId(hhmLocalId: Long, neonateId: Long) {
        pregnancyDetailDao.updateNeonatePatientId(hhmLocalId, neonateId)
    }

    override suspend fun changeHHMLinkCallStatus(idList: List<String>, syncStatus: String) {
        callHistoryDao.updateInProgress(idList, syncStatus)
    }

    override suspend fun changeCommunityProfileStatus(idList: List<Long>, syncStatus: String) {
        communityDAO.updateInStatus(idList, syncStatus)
    }

    override suspend fun changeAssignHHMStatus(idList: List<String>, syncStatus: String) {
        linkHouseholdMemberDao.updateInProgress(idList, syncStatus)
    }

    override suspend fun getMemberDetailsByPatientId(patientId: String): HouseholdMemberEntity? {
       return memberDAO.getMemberDetailsByPatientId(patientId)
    }

    override suspend fun getChildPatientId(parentId: Long): Long? {
        return pregnancyDetailDao.getChildPatientId(parentId)
    }

    override suspend fun getPatientIdById(id: Long): String {
        return memberDAO.getPatientIdById(id)
    }

    override suspend fun insertConsentForm(form: ConsentForm): Long {
        return consentFormDao.insert(form)
    }
    override suspend fun getConsentFormByType(type: String): ConsentForm? {
        return consentFormDao.getConsentFormByType(type)
    }

    override suspend fun deleteAllConsentForm() {
        consentFormDao.delete()
    }

    override suspend fun getHHSignatureDetails(): List<HHSignatureDetail> {
        return memberDAO.getHHSignatureDetails()
    }
    override suspend fun updatePhoneNumberForHouseholdHead(id: Long, phoneNumber: String?, phoneNumberCategory: String?) {
        return memberDAO.updatePhoneNumberForHouseholdHead(id,phoneNumber, phoneNumberCategory)
    }

    override suspend fun insertLinkHouseholdMembers(insertList: List<LinkHouseholdMember>) {
        linkHouseholdMemberDao.insert(insertList)
    }

    override suspend fun deleteLinkHouseholdMembersById(deleteListIds: List<String>) {
        linkHouseholdMemberDao.delete(deleteListIds)
    }

    override fun getUnAssignedHouseholdMembersLiveData(): LiveData<List<UnAssignedHouseholdMemberDetail>> {
        return linkHouseholdMemberDao.getUnAssignedHouseholdMembersLiveData()
    }

    override suspend fun addLinkMemberCall(callHistory: CallHistory): Long {
        return callHistoryDao.insert(callHistory)
    }

    override suspend fun getUnSyncedCallHistoryForHHMLink(): List<HouseholdMemberCallRegisterDto> {
        return callHistoryDao.getUnSyncedCallHistoryForHHMLink()
    }

    override suspend fun changeMemberDetailsToNotSynced(id: Long) {
        memberDAO.changeMemberDetailsToNotSynced(id)
    }

    override suspend fun updateMemberAsAssigned(memberId: String) {
        linkHouseholdMemberDao.updateMemberAsAssigned(memberId)
    }

    override suspend fun saveMedicalCompliance(list: List<MedicalComplianceEntity>) {
        metaDataDAO.insertMedicalCompliance(list)
    }

    override suspend fun getMedicalParentComplianceList(): List<MedicalComplianceEntity> {
        return metaDataDAO.getMedicalComplianceList()
    }

    override suspend fun getMedicalChildComplianceList(parentId: Long): List<MedicalComplianceEntity> {
        return metaDataDAO.getMedicalComplianceList(parentId)
    }

    override suspend fun deleteMedicalCompliance() {
        metaDataDAO.deleteMedicalComplianceList()
    }

    override suspend fun saveDistricts(districts: List<DistrictEntity>) {
        metaDataDAO.insertDistricts(districts)
    }

    override suspend fun getDistricts(countryId: Long): List<DistrictEntity> {
        return metaDataDAO.getDistricts(countryId)
    }

    override suspend fun deleteDistricts() {
        metaDataDAO.deleteCounties()
    }

    override suspend fun saveChiefDoms(chiefdoms: List<ChiefDomEntity>) {
        metaDataDAO.insertChiefDoms(chiefdoms)
    }

    override suspend fun getChiefDoms(districtId: Long): List<ChiefDomEntity> {
        return metaDataDAO.getChiefDoms(districtId)
    }

    override suspend fun deleteChiefDoms() {
        metaDataDAO.deleteChiefDoms()
    }

    override suspend fun savePrograms(programs: List<ProgramEntity>) {
        metaDataDAO.insertPrograms(programs)
    }

    override suspend fun getPrograms(): List<ProgramEntity> {
        return metaDataDAO.getPrograms()
    }

    override suspend fun deletePrograms() {
        metaDataDAO.deletePrograms()
    }

    override fun getMentalQuestion(formType: String): LiveData<MentalHealthEntity?> {
        return metaDataDAO.getMentalQuestion(formType)
    }

    override fun getSites(): LiveData<List<HealthFacilityEntity>> {
        return metaDataDAO.getSites()
    }

    override suspend fun savePatientScreeningInformation(screeningEntity: ScreeningEntity): ScreeningEntity {
        val id = screeningDAO.insertScreening(screeningEntity)
        return screeningDAO.getScreeningById(id)
    }

    override fun getScreenedPatientCount(
        startDate: Long,
        endDate: Long,
        userId: String
    ): LiveData<Long> {
        return screeningDAO.getScreenedPatientCount(startDate, endDate, userId)
    }

    override fun getScreenedPatientReferredCount(
        startDate: Long,
        endDate: Long,
        userId: String,
        isReferred: Boolean
    ): LiveData<Long> {
        return screeningDAO.getScreenedPatientReferredCount(startDate, endDate,userId, isReferred)
    }

    override suspend fun getAllScreeningRecords(uploadStatus: Boolean): List<ScreeningEntity> {
        return screeningDAO.getAllScreeningRecords(uploadStatus)
    }
    override suspend fun deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds: Long) {
        return screeningDAO.deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds)
    }

    override suspend fun updateScreeningRecordById(id: Long, uploadStatus: Boolean) {
        return screeningDAO.updateScreeningRecordById(id, uploadStatus)
    }

    override fun getAssessmentFormData(formType: String, workFlow: String): LiveData<String> {
        return metaDataDAO.getAssessmentFormData(formType, workFlow)
    }

    override suspend fun insertRiskFactor(riskFactorEntity: RiskFactorEntity) {
        riskFactorDAO.insertRiskFactor(riskFactorEntity)
    }

    override fun getRiskFactorEntity(): LiveData<List<RiskFactorEntity>> {
        return riskFactorDAO.getAllRiskFactorEntity()
    }

    override suspend fun deleteRiskFactor() {
        return riskFactorDAO.deleteRiskFactor()
    }

    override fun getSymptomListByTypeForNCD(type: String): LiveData<List<SignsAndSymptomsEntity>> {
        return assessmentDAO.getSymptomListByTypeForNCD(type)
    }

    override suspend fun deleteTreatmentPlan() {
        return ncdMedicalReviewDao.deleteTreatmentPlan()
    }

    override suspend fun insertTreatmentPlan(items: List<TreatmentPlanEntity>) {
        return ncdMedicalReviewDao.insertTreatmentPlan(items)
    }

    override suspend fun deleteNCDMedicalReviewMeta() {
        return ncdMedicalReviewDao.deleteNCDMedicalReviewMeta()
    }

    override suspend fun insertNCDMedicalReviewMeta(items: List<NCDMedicalReviewMetaEntity>) {
        return ncdMedicalReviewDao.insertNCDMedicalReviewMeta(items)
    }

    override fun getComorbidities(type: String?,category: String): LiveData<List<NCDMedicalReviewMetaEntity>> {
       return  ncdMedicalReviewDao.getComorbidities(type,category)
    }

    override suspend fun deleteLifestyle() {
        return ncdMedicalReviewDao.deleteLifestyle()
    }

    override suspend fun insertLifestyle(items: List<LifestyleEntity>) {
        return ncdMedicalReviewDao.insertLifestyle(items)
    }

    override fun getLifeStyle(): LiveData<List<LifestyleEntity>> {
        return ncdMedicalReviewDao.getLifeStyle()
    }

    override fun getAssessmentFormData(
        formTypes: List<String>,
        workFlow: String
    ): List<String> {
        return metaDataDAO.getAssessmentFormData(formTypes, workFlow)
    }

    override suspend fun getSymptomList(): List<SignsAndSymptomsEntity> {
        return assessmentDAO.getSymptomList()
    }

    override suspend fun saveAssessmentInformation(assessmentOfflineEntity: AssessmentNCDEntity):
            AssessmentNCDEntity {
        val id = assessmentDAO.saveAssessmentInformation(assessmentOfflineEntity)
        return assessmentDAO.getAssessmentById(id)
    }

    override suspend fun getAllAssessmentRecords(uploadStatus: Boolean): List<AssessmentNCDEntity> {
        return assessmentDAO.getAllAssessmentRecords(uploadStatus)
    }

    override suspend fun updateAssessmentUploadStatus(id: Long, uploadStatus: Boolean) {
        return assessmentDAO.updateAssessmentUploadStatus(id, uploadStatus)
    }

    override suspend fun deleteAssessmentList(isUploaded: Boolean) = assessmentDAO.deleteAssessmentList()
    override suspend fun getAssessmentClinicalWorkflow(
        gender: String,
        name: String
    ): List<NCDAssessmentClinicalWorkflow> {
        return metaDataDAO.getAssessmentClinicalWorkflow(gender, name)
    }

    override fun getUnSyncedDataCountForNCDScreening(): LiveData<Long> {
        return screeningDAO.getUnSyncedDataCountForNCDScreening()
    }

    override fun getUnSyncedNCDAssessmentCount(): LiveData<Long> {
        return assessmentDAO.getUnSyncedNCDAssessmentCount()
    }

    override suspend fun saveNCDDiagnosisList(diseaseEntityList: ArrayList<NCDDiagnosisEntity>) {
        return ncdMedicalReviewDao.saveNCDDiagnosisList(diseaseEntityList)
    }

    override suspend fun deleteNCDDiagnosisList() {
        return ncdMedicalReviewDao.deleteNCDDiagnosisList()
    }

    override fun getNCDDiagnosisList(
        types: List<String>,
        gender: String,
        isPregnant: Boolean
    ): LiveData<List<NCDDiagnosisEntity>> {
        return ncdMedicalReviewDao.getNCDDiagnosisList(types, gender, isPregnant)
    }

    override fun getFrequencies(): LiveData<List<TreatmentPlanEntity>> {
        return ncdMedicalReviewDao.getFrequencies()
    }

    override suspend fun getNCDShortageReason(type: String): List<ShortageReasonEntity> {
        return ncdMedicalReviewDao.getNCDShortageEntries(type)
    }

    override suspend fun deleteNCDShortageReason() {
        return ncdMedicalReviewDao.deleteNCDShortageReason()
    }

    override suspend fun saveNCDShortageReason(shortageReasonEntity: List<ShortageReasonEntity>) {
         return ncdMedicalReviewDao.saveNCDShortageReason(shortageReasonEntity)
    }

    override suspend fun getNCDForm(type: String, customizedType: String): List<String> {
        return metaDataDAO.getNCDForm(type, customizedType)
    }

    override suspend fun getUserVillages(): List<VillageEntity> {
        return metaDataDAO.getUserVillages(true)
    }

    override suspend fun deleteDosageDurations() {
        return metaDataDAO.deleteDosageDurations()
    }

    override suspend fun insertDosageDurations(items: List<DosageDurationEntity>) {
        return metaDataDAO.insertDosageDurations(items)
    }

    override suspend fun getDosageDurations(): List<DosageDurationEntity> {
        return metaDataDAO.getDosageDurationsList()
    }

    override suspend fun getUnitList(type: String): List<UnitMetricEntity> {
        return metaDataDAO.getUnitList(type)
    }
    override suspend fun saveUnitMetric(list: ArrayList<UnitMetricEntity>) {
        metaDataDAO.insertUnitMetricList(list)
    }

    override suspend fun getDosageFrequencyList(): List<DosageFrequency> {
        return metaDataDAO.getDosageFrequencyList()
    }

    override suspend fun deleteUnitMetric() {
        metaDataDAO.deleteUnitMetric()
    }

    override suspend fun saveDosageFrequencyList(list: ArrayList<DosageFrequency>) {
        metaDataDAO.insertDosageFrequencyList(list)
    }

    override suspend fun deleteDosageFrequencyList() {
        metaDataDAO.deleteDosageFrequencyList()
    }

    override suspend fun getUnAssignedChildFhirIds(patientId: String): List<String> {
        return linkHouseholdMemberDao.getUnAssignedChildFhirIds(patientId)
    }

    override suspend fun getUnAssignedParentFhirId(parentId: String): List<String> {
        return linkHouseholdMemberDao.getUnAssignedParentFhirId(parentId)
    }

    override suspend fun updateHouseholdHeadAndRelationShip(
        fhirIds: List<String>,
        householdId: Long
    ) {
        memberDAO.updateHouseholdHeadAndRelationShip(fhirIds, householdId)
    }

    override suspend fun updateMembersAsAssigned(fhirIds: List<String>) {
        linkHouseholdMemberDao.updateMembersAsAssigned(fhirIds)
    }
    override suspend fun deleteAllNCDFollowUp() {
        ncdFollowUpDao.deleteAllNCDFollowUps()
    }
    override suspend fun insertNCDFollowUp(followUp: NCDFollowUp): Long {
        return ncdFollowUpDao.insertNCDFollowUp(followUp)
    }

    override fun getNCDFollowUpData(
        type: String,
        searchText: String,
        dateBasedOnChip: Pair<Long?, Long?>?,
        isScreened: Boolean?,
        reason: String?
    ): LiveData<List<NCDFollowUp>> {
        return ncdFollowUpDao.getFilteredNCDFollowUp(
            type,
            searchText,
            dateBasedOnChip?.first,
            dateBasedOnChip?.second,
            isScreened,
            reason,
            DateUtils.dateToLong()
        )
    }

    override suspend fun updatedCallInitiatedCall(ncdFollowUp: NCDFollowUp): NCDFollowUp {
        ncdFollowUpDao.updatedCallInitiated(ncdFollowUp) // Perform the update/insert
        return ncdFollowUpDao.getNCDFollowUpById(ncdFollowUp.id) // Retrieve the updated object
    }


    override suspend fun getNCDInitiatedCallFollowUp(): NCDFollowUp? {
        return ncdFollowUpDao.getNCDInitiatedCallFollowUp()
    }

    @Transaction
    override suspend fun insertNCDCallDetails(followUp: NCDCallDetails): NCDCallDetails? {
        val id = ncdFollowUpDao.insertNCDCallDetails(followUp)
        if (followUp.reason == FollowUpDefinedParams.WRONG_NUMBER) {
            // Update isWrongNumber in NCDFollowUp
            ncdFollowUpDao.markAsWrongNumber(followUp.id)
        }
        ncdFollowUpDao.updateNCDInitiatedCallFollowUp(followUp.id)
        return ncdFollowUpDao.getNCDCallDetails(id)
    }

    override suspend fun updateRetryAttempts(id: Long, retryAttempts: Long) {
        return ncdFollowUpDao.updateRetryAttempts(id, retryAttempts)
    }

    override suspend fun getAttemptsById(id: Long): Long? {
        return ncdFollowUpDao.getAttemptsById(id)
    }

    override suspend fun getNCDFollowUpById(id: Long): NCDFollowUp {
        return ncdFollowUpDao.getNCDFollowUpById(id)
    }

    override suspend fun getAllNCDCallDetails(): List<NCDCallDetails> {
        return ncdFollowUpDao.getAllNCDCallDetails()
    }

    override suspend fun insertNCDPatientDetails(patients: NCDPatientDetailsEntity): Long {
        return ncdFollowUpDao.insertNCDPatientDetails(patients)
    }

    override suspend fun deleteAllNCDPatientDetails() {
        return ncdFollowUpDao.deleteAllNCDPatientDetails()
    }

    override suspend fun getPatientBasedOnId(id: String): NCDPatientDetailsEntity {
        return ncdFollowUpDao.getPatientBasedOnId(id)
    }

    override suspend fun deleteCallDetails(id: Long) {
        return ncdFollowUpDao.deleteCallDetails(id)
    }

    override fun getUnSyncedNCDFollowUpCount(): LiveData<Long> {
        return ncdFollowUpDao.getUnSyncedNCDFollowUpCount()
    }

    override suspend fun saveCultures(cultures: List<CulturesEntity>) {
        metaDataDAO.insertCultures(cultures)
    }

    override suspend fun getCultures(): List<CulturesEntity> {
        return metaDataDAO.getCultures()
    }

    override suspend fun deleteCultures() {
        metaDataDAO.deleteCultures()
    }

    override suspend fun insertLinkedVillages(linkedVillages: List<LinkedVillageEntity>) {
        metaDataDAO.insertLinkedVillages(linkedVillages)
    }

    override suspend fun deleteAllLinkedVillages() {
        metaDataDAO.deleteAllLinkedVillages()
    }

    override suspend fun updateMemberDeceasedReason(id: Long, status: Boolean,deceasedReason: String?) {
        memberDAO.updateMemberDeceasedReason(id, status, OfflineSyncStatus.NotSynced,deceasedReason)
    }

    override suspend fun getHouseholdHeadDob(householdId: Long): String {
       return memberDAO.getHouseholdHeadDob(householdId)
    }

    override fun getFilterVillagesWithHouseholdsCount(searchInput: String): LiveData<List<CommunityProfileDetail>> {
        return metaDataDAO.filterCommunityProfile(searchInput)
    }

    override suspend fun getCommunityStatistics(villageId: Long): CommunityPopulationStatistics {
        return metaDataDAO.getCommunityPopulationStatistics(villageId)
    }

    override suspend fun insertCommunityDetails(communityProfile: CommunityProfile): Long {
        return communityDAO.insertCommunity(communityProfile)
    }

    override suspend fun getCommunityDetails(id: Long): CommunityProfile? {
        return communityDAO.getCommunityDetailsById(id)
    }

    override suspend fun getCommunityProfileId(villageId: Long): Long? {
        return communityDAO.getCommunityProfileId(villageId)
    }

    override suspend fun updateCommunityDetails(communityProfile: CommunityProfile){
        return communityDAO.updateCommunity(communityProfile)
    }

    override suspend fun updateUnSynStatus(villageId: Long, synStatus: String) {
        return communityDAO.updateSyncStatus(villageId,synStatus  )
    }

    override suspend fun getHealthFacilityBasedOnVillageId(villageId: Long): List<HealthFacilityEntity> {
        return metaDataDAO.getHealthFacilityBasedOnVillageId(villageId)
    }

    override suspend fun getAssessment(assessmentId: Long): AssessmentEntity {
        return assessmentDAO.getAssessment(assessmentId)
    }

    override suspend fun getUnSyncedCommunityDetails(): List<CommunityProfile> {
        return communityDAO.getUnSyncedCommunityDetails()
    }

    override suspend fun insertOrUpdateFromBE(communityProfile: CommunityProfile): Long {
        return communityDAO.insertOrUpdateFromBE(communityProfile)
    }

    override fun householdMemberWithTbStatus(hhId: Long): LiveData<List<HouseholdMemberEntity>> {
        return memberDAO.getHouseholdMemberWithTBContactTraceStatus(hhId)
    }

    override suspend fun updateTBContactTraceStatus(hhmId: Long, tbContactTracingStatus: Int){
        return memberDAO.updateTBContactTraceStatus(hhmId,tbContactTracingStatus)
    }
}