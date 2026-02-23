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
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberFhirId
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberStatus
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberWithTb
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdWithMemberCount
import com.medtroniclabs.spice.data.offlinesync.model.RxBuddyFollowUpDetails
import com.medtroniclabs.spice.data.offlinesync.model.RxBuddyRegisterDetail
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
import com.medtroniclabs.spice.db.dao.HivMetaDataDAO
import com.medtroniclabs.spice.db.dao.HouseholdDAO
import com.medtroniclabs.spice.db.dao.LabourDeliveryDAO
import com.medtroniclabs.spice.db.dao.LinkHouseholdMemberDao
import com.medtroniclabs.spice.db.dao.MemberDAO
import com.medtroniclabs.spice.db.dao.MetaDataDAO
import com.medtroniclabs.spice.db.dao.NCDFollowUpDao
import com.medtroniclabs.spice.db.dao.NcdMedicalReviewDao
import com.medtroniclabs.spice.db.dao.PregnancyDetailDao
import com.medtroniclabs.spice.db.dao.RiskFactorDAO
import com.medtroniclabs.spice.db.dao.RxBuddyDetailsDAO
import com.medtroniclabs.spice.db.dao.RxBuddyFollowUpDAO
import com.medtroniclabs.spice.db.dao.ScreeningDAO
import com.medtroniclabs.spice.db.dao.TreatmentDetailsDAO
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
import com.medtroniclabs.spice.db.entity.EntitiesName
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
import com.medtroniclabs.spice.db.entity.RxBuddyDetails
import com.medtroniclabs.spice.db.entity.RxBuddyFollowUpEntity
import com.medtroniclabs.spice.db.entity.ScreeningEntity
import com.medtroniclabs.spice.db.entity.ShasthyaShebikaEntity
import com.medtroniclabs.spice.db.entity.ShasthyaShebikaLinkedVillageEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.SubVillageEntity
import com.medtroniclabs.spice.db.entity.TreatmentDetailsEntity
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
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
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
    private val communityDAO: CommunityDetailsDAO,
    private val rxBuddyDetailsDAO: RxBuddyDetailsDAO,
    private val treatmentDetailsDAO: TreatmentDetailsDAO,
    private val rxBuddyFollowUpDAO: RxBuddyFollowUpDAO,
    private val hivMetaDataDAO: HivMetaDataDAO,
) : RoomHelper {
    override suspend fun saveHouseHoldEntry(householdEntity: HouseholdEntity): Long = householdDAO.insertHouseHold(householdEntity)

    override suspend fun updateHousehold(householdEntity: HouseholdEntity) = householdDAO.updateHouseHold(householdEntity)

    override suspend fun getLastHouseholdNo(villageId: Long): Long? = householdDAO.getLastHouseholdNo(villageId)

    override suspend fun checkHouseholdNumberExists(householdNo: Long): Boolean = householdDAO.checkHouseholdNumberExists(householdNo) > 0

    override suspend fun getHouseHoldDetailsById(houseHoldId: Long): HouseholdEntity = householdDAO.getHouseHoldDetailsById(houseHoldId)

    override fun getMemberCountInHouseholdLiveData(houseHoldId: Long): LiveData<HouseholdMemberCount> =
        householdDAO.getHouseholdMemberCountLiveData(houseHoldId)

    override suspend fun registerMember(householdMemberEntity: HouseholdMemberEntity): Long = memberDAO.insertMember(householdMemberEntity)

    override suspend fun getAllHouseHoldMemberList(houseHoldId: Long): ArrayList<HouseholdMemberEntity> =
        ArrayList(memberDAO.getAllHouseHoldMemberList(houseHoldId))

    override suspend fun getMemberDetailsByID(memberId: Long): HouseholdMemberEntity = memberDAO.getMemberDetailsById(memberId)

    override suspend fun getMemberDetailsByParentId(memberId: String): List<HouseholdMemberEntity> = memberDAO.getMemberDetailsByParentId(memberId)

    override suspend fun getMemberCountPerHouseHold(householdId: Long): Int = memberDAO.getMemberCountPerHouseHold(householdId)

    override suspend fun getLastPatientId(patientIdStarts: String): String? = memberDAO.getLastPatientId(patientIdStarts)

    override suspend fun getAllUnSyncedHouseHolds(hhIds: List<String>): List<HouseHold> = householdDAO.getAllUnSyncedHouseHolds(hhIds)

    override suspend fun getAllUnSyncedHouseHoldMembers(
        houseHoldId: Long,
        memberIds: List<Long>,
    ): List<HouseHoldMember> = memberDAO.getAllUnSyncedHouseHoldMembers(houseHoldId, memberIds)

    override suspend fun getOtherHouseholdMembers(memberIds: List<String>): List<HouseHoldMember> = memberDAO.getOtherHouseholdMembers(memberIds)

    override suspend fun saveAssessment(assessmentEntity: AssessmentEntity): Long = assessmentDAO.insertAssessment(assessmentEntity)

    override suspend fun updateOtherAssessmentDetails(assessmentEntity: AssessmentEntity) = assessmentDAO.updateOtherAssessmentDetails(assessmentEntity)

    override suspend fun getLatestAssessmentForMember(memberId: Long): AssessmentEntity? = assessmentDAO.getLatestAssessmentForMember(memberId)

    override suspend fun insertSymptomList(symptoms: List<SignsAndSymptomsEntity>) {
        assessmentDAO.insertSymptoms(symptoms)
    }

    override suspend fun getSymptomListByType(type: String): List<SignsAndSymptomsEntity> = assessmentDAO.getSymptomListByType(type)

    override suspend fun saveHealthFacility(healthFacilityEntityList: HealthFacilityEntity) {
        metaDataDAO.insertHealthFacility(healthFacilityEntityList)
    }

    override suspend fun updateHeadCount(
        householdId: Long,
        newNoOfPeople: Int,
    ) = householdDAO.updateHeadCount(householdId, newNoOfPeople)

    override suspend fun deleteAllHealthFacility() {
        metaDataDAO.deleteAllHealthFacility()
    }

    override suspend fun saveVillage(villageEntityList: List<VillageEntity>) {
        metaDataDAO.insertVillages(villageEntityList)
    }

    override suspend fun getAllVillageEntity(): List<VillageEntity> = metaDataDAO.getVillages()

    override suspend fun getAllLinkedVillageEntity(): List<VillageEntity> = metaDataDAO.getLinkedVillages(SecuredPreference.getTenantId())

    override suspend fun getVillagesByChiefDom(chiefdomId: Long): List<VillageEntity> = metaDataDAO.getVillagesByChiefDom(chiefdomId)

    override suspend fun saveSubVillages(subVillageEntityList: List<SubVillageEntity>) {
        metaDataDAO.insertSubVillages(subVillageEntityList)
    }

    override suspend fun deleteAllSubVillages() {
        metaDataDAO.deleteAllSubVillages()
    }

    override suspend fun saveShasthyaShebikas(shasthyaShebikaEntityList: List<ShasthyaShebikaEntity>) {
        metaDataDAO.insertShasthyaShebikas(shasthyaShebikaEntityList)
    }

    override suspend fun deleteAllShasthyaShebikas() {
        metaDataDAO.deleteAllShasthyaShebikas()
    }

    override suspend fun getShasthyaShebikaByShasthyaKormiId(shasthyaKormiId: Long): List<ShasthyaShebikaEntity> =
        metaDataDAO.getShasthyaShebikaByShasthyaKormiId(shasthyaKormiId)

    override suspend fun insertShasthyaShebikaLinkedVillages(linkedVillages: List<ShasthyaShebikaLinkedVillageEntity>) {
        metaDataDAO.insertShasthyaShebikaLinkedVillages(linkedVillages)
    }

    override suspend fun deleteAllShasthyaShebikaLinkedVillages() {
        metaDataDAO.deleteAllShasthyaShebikaLinkedVillages()
    }

    override suspend fun getSubVillagesByShasthyaShebikaId(shasthyaShebikaId: Long): List<SubVillageEntity> =
        metaDataDAO.getSubVillagesByShasthyaShebikaId(shasthyaShebikaId)

    override suspend fun getDefaultHealthFacility(): HealthFacilityEntity? = metaDataDAO.getDefaultHealthFacility()

    override suspend fun getClinicalWorkflowId(
        gender: String,
        age: Int,
    ): List<NCDAssessmentClinicalWorkflow> = metaDataDAO.getClinicalWorkflowId(gender, age, MenuTypeEnums.assessment.name)

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

    override suspend fun saveClinicalWorkflows(clinicalWorkflows: List<ClinicalWorkflowEntity>) = metaDataDAO.saveClinicalWorkflows(clinicalWorkflows)

    override suspend fun deleteAllClinicalWorkflow() = metaDataDAO.deleteAllClinicalWorkflow()

    override suspend fun saveForms(forms: List<FormEntity>) = metaDataDAO.saveForms(forms)

    override suspend fun deleteAllForms() = metaDataDAO.deleteAllForms()

    override suspend fun getAllClinicalWorkflowIds(): List<Int> = metaDataDAO.getAllClinicalWorkflowIds()

    override suspend fun insertSymptoms(symptomEntity: List<SignsAndSymptomsEntity>) {
        metaDataDAO.insertSymptoms(symptomEntity)
    }

    override suspend fun deleteAllSymptoms() {
        metaDataDAO.deleteAllSymptoms()
    }

    override suspend fun getFormData(formType: String): String = metaDataDAO.getFormData(formType)

    override suspend fun deleteAllMenus() {
        metaDataDAO.deleteAllMenus()
    }

    override suspend fun saveUserProfileDetails(userProfileEntity: UserProfileEntity) {
        metaDataDAO.insertUserProfileDetails(userProfileEntity)
    }

    override suspend fun deleteAllUserProfileDetails() {
        metaDataDAO.deleteAllUserProfileDetails()
    }

    override suspend fun getMenus(): List<MenuEntity> = metaDataDAO.getMenus()

    override suspend fun getUserProfile(): UserProfileEntity = metaDataDAO.getUserProfile()

    override suspend fun getVillageByID(villageId: Long): VillageEntity = metaDataDAO.getVillageByID(villageId)

    override suspend fun getMenuForClinicalWorkflows(): List<ClinicalWorkflowEntity> = metaDataDAO.getMenuForClinicalWorkflows()

    override suspend fun deleteClinicalWorkflowConditions() {
        metaDataDAO.deleteClinicalWorkflowConditions()
    }

    override suspend fun insertClinicalWorkflowConditions(clinicalWorkflowConditions: List<ClinicalWorkflowConditionEntity>) {
        metaDataDAO.insertClinicalWorkflowConditions(clinicalWorkflowConditions)
    }

    override suspend fun getDobAndGenderById(memberId: Long): MemberDobGenderModel = memberDAO.getDobAndGenderById(memberId)

    override suspend fun updateFhirId(
        tableName: String,
        id: String,
        fhirId: String?,
        status: String,
    ) {
        when (tableName) {
            EntitiesName.RX_BUDDY -> {
                fhirId?.let {
                    rxBuddyDetailsDAO.updateRxBuddyId(id = id.toLong(), rxBuddyId = it.toLong())
                    rxBuddyFollowUpDAO.updateRxBuddyId(id = id.toLong(), rxBuddyId = it.toLong())
                }
            }

            else -> {
                val updatedAt = System.currentTimeMillis()
                val query =
                    "UPDATE $tableName SET fhir_id = ?, updated_at = ?, sync_status = CASE WHEN sync_status = 'InProgress' THEN ? WHEN sync_status = 'NetworkError' THEN ? ELSE sync_status END WHERE id = ?"
                householdDAO.updateFhirId(
                    SimpleSQLiteQuery(
                        query,
                        arrayOf(fhirId, updatedAt, status, status, id),
                    ),
                )
            }
        }
    }

    override fun getFilteredHouseholdsLiveData(
        searchInput: String,
        filterByVillage: List<Long>,
        filterBySs: List<Long>,
        filterByStatus: String,
    ): LiveData<List<HouseHoldEntityWithMemberCount>> =
        if (filterByVillage.isEmpty() && filterBySs.isEmpty()) {
            householdDAO.getHouseholdsWithFilterLiveData(searchInput, filterByStatus)
        } else if (filterBySs.isEmpty()) {
            householdDAO.getHouseholdsWithFilterLiveData(
                searchInput,
                filterByStatus,
                filterByVillage,
            )
        } else if (filterByVillage.isEmpty()) {
            householdDAO.getHouseHoldsWithStatusAndSsFilterLiveData(
                searchInput,
                filterByStatus,
                filterBySs,
            )
        } else {
            householdDAO.getHouseholdsWithFilterLiveData(
                searchInput,
                filterByStatus,
                filterByVillage,
                filterBySs,
            )
        }

    override suspend fun getUnSyncedHouseholdCount(): Int = householdDAO.getUnSyncedCount()

    override suspend fun getUnSyncedHouseholdMemberCount(): Int = memberDAO.getUnSyncedCount()

    override suspend fun getNearestHealthFacility(): List<HealthFacilityEntity> = metaDataDAO.getNearestHealthFacility()

    override suspend fun getPatientVisitCountByType(
        type: String,
        hhmLocalId: Long,
    ): MemberClinicalEntity? =
        when (type) {
            RMNCH.ANC -> pregnancyDetailDao.getAncDetail(hhmLocalId)
            RMNCH.PNC -> pregnancyDetailDao.getPncDetail(hhmLocalId)
            else -> pregnancyDetailDao.getChildhoodVisitDetail(hhmLocalId)
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

    override suspend fun getHouseholdIdByFhirId(fhirId: String?): Long? =
        if (fhirId != null) {
            householdDAO.getHouseholdIdByFhirId(fhirId)
        } else {
            null
        }

    override suspend fun getHouseholdMemberIdByFhirId(fhirId: String?): Long? {
        return if (fhirId != null) {
            memberDAO.getHouseholdMemberIdByFhirId(fhirId)
        } else {
            return null
        }
    }

    override suspend fun getExaminationsComplaintByType(type: String): List<MedicalReviewMetaItems> =
        examinationsComplaintsDAO.getExaminationsComplaintByType(type)

    override suspend fun getAssessmentMemberDetails(id: Long): AssessmentMemberDetails = memberDAO.getAssessmentMemberDetails(id)

    override suspend fun getOtherUnSyncedAssessments(addedAssessmentIds: List<String>): List<AssessmentDetails> =
        assessmentDAO.getOtherUnSyncedAssessments(addedAssessmentIds)

    override suspend fun getUnSyncedAssessmentByHHMId(hhmId: Long): List<AssessmentDetails> = assessmentDAO.getUnSyncedAssessmentByHHMId(hhmId)

    override suspend fun getUnSyncedAssessmentCount(): Int = assessmentDAO.getUnSyncedCount()

    override suspend fun updatePregnancyAncDetail(
        hhmLocalId: Long,
        visitCount: Long,
        clinicalDate: String?,
    ) {
        pregnancyDetailDao.updatePregnancyAnc(hhmLocalId)
    }

    override suspend fun getSummaryDetailMetaItems(type: String): List<MedicalReviewMetaItems> = aboveFiveYearsDAO.getSummaryDetailMetaItems(type)

    override suspend fun deleteExaminationsComplaintsForAnc(type: String) {
        examinationsComplaintsDAO.deleteExaminationsComplaintsForAnc(type)
    }

    override fun getExaminationsComplaintsForAnc(
        category: String,
        type: String,
    ): LiveData<List<MedicalReviewMetaItems>> = examinationsComplaintsDAO.getExaminationsComplaintsForAnc(category, type)

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

    override suspend fun getLabourDelivery(): List<LabourDeliveryMetaEntity> = labourDeliveryDAO.getLabourDelivery()

    override suspend fun getDiagnosisList(diagnosisType: String): List<DiseaseCategoryItems> = diagnosisDAO.getDiagnosisList(diagnosisType)

    override suspend fun insertFollowUp(followUp: FollowUp): Long = followUpDao.insertFollowUp(followUp)

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
        toDate: String,
    ): LiveData<List<FollowUpPatientModel>> {
        if (type == FollowUpDefinedParams.FU_TYPE_REFERRED) {
            return followUpDao.getReferredFollowUpPatientListLiveData(
                type = type,
                search = search,
                villageIds = villageIds,
                fromDate = fromDate,
                toDate = toDate,
            )
        } else {
            return followUpDao.getOtherFollowUpPatientListLiveData(
                type = type,
                search = search,
                villageIds = villageIds,
                fromDate = fromDate,
                toDate = toDate,
            )
        }
    }

    override suspend fun getAllVillageIds(): List<Long> = metaDataDAO.getVillageIds()

    override suspend fun getExaminationQuestionsByWorkFlow(workFlowType: String): ExaminationListItems = examinationsDAO.getExaminationsByType(workFlowType)

    override suspend fun getPatientIdByFhirId(fhirId: String): String? = memberDAO.getPatientIdByFhirId(fhirId)

    override suspend fun deleteAllPregnancyDetails() {
        pregnancyDetailDao.deleteAllPregnancyDetails()
    }

    override suspend fun insertUpdatePregnancyDetailFromBE(pregnancyDetail: PregnancyDetail) {
        pregnancyDetailDao.insertOrUpdateFromBE(pregnancyDetail)
    }

    override suspend fun addCallHistory(
        oldFollowUp: FollowUp,
        history: FollowUpCall,
        newFollowUp: FollowUp?,
    ) {
        followUpCallsDao.insertFollowUpCall(history)
        followUpDao.insertFollowUp(oldFollowUp)
        newFollowUp?.let {
            followUpDao.insertFollowUp(it)
        }
    }

    override suspend fun getAllFollowUpRequests(): List<FollowUp> = followUpDao.getAllFollowUps()

    override suspend fun getAllFollowUpCalls(id: Long): List<FollowUpCall> = followUpCallsDao.getAllFollowUpCalls(id)

    override suspend fun getFollowUpById(id: Long): FollowUp = followUpDao.getFollowUpDetailsById(id)

    override suspend fun deleteAllFollowUpCalls() = followUpCallsDao.deleteAllFollowUpCalls()

    override suspend fun getUnSyncedFollowUpCount(): Int = followUpDao.getUnSyncedCount()

    override suspend fun getUnSyncedCommunityProfileCount(): Int = communityDAO.getUnSyncedCount()

    override suspend fun deleteAllAssessments() = assessmentDAO.deleteAllAssessments()

    override fun getExaminationsComplaintByTypeLiveData(category: String): LiveData<List<MedicalReviewMetaItems>> =
        examinationsComplaintsDAO.getExaminationsComplaintByTypeLiveData(category)

    override fun getHouseholdCardDetailLiveData(id: Long): LiveData<HouseholdCardDetail> = householdDAO.getHouseholdCardDetailLiveData(id)

    override fun getAllHouseHoldMembersLiveData(hhId: Long): LiveData<List<HouseholdMemberWithTb>> = memberDAO.getAllHouseHoldMembersLiveData(hhId)

    override fun getAliveHouseHoldMembersLiveData(hhId: Long): List<HouseholdMemberEntity> = memberDAO.getAliveHouseHoldMembers(hhId, true)

    override suspend fun updateOtherDuplicateTickets(
        id: Long,
        followUp: FollowUp,
    ) {
        followUpDao.updateOtherDuplicateTickets(
            id,
            followUp.memberId,
            followUp.type,
            followUp.encounterType,
            followUp.reason,
        )
    }

    override suspend fun updateDuplicateTicketsAsCompleted(
        id: Long,
        followUp: FollowUp,
    ) {
        if (followUp.type == FollowUpDefinedParams.FU_TYPE_HH_VISIT) {
            followUpDao.updateHHVisitTicketsOnRecovered(id, followUp.memberId, followUp.type, followUp.encounterType, followUp.reason)
        } else {
            val types = listOf(FollowUpDefinedParams.FU_TYPE_REFERRED, FollowUpDefinedParams.FU_TYPE_MEDICAL_REVIEW)
            if (followUp.encounterType == FollowUpDefinedParams.FU_ENCOUNTER_TYPE_RMNCH) {
                followUpDao.closeTicketsForRMNCH(id, followUp.memberId, types, followUp.encounterType)
            } else {
                followUpDao.closeTicketsForNonRMNCH(id, followUp.memberId, followUp.type, types, followUp.encounterType)
            }
        }
    }

    override suspend fun updateOnTreatmentStatus(
        id: Long,
        followUp: FollowUp,
        updateAt: Long,
    ) {
        followUpDao.updateOnTreatmentStatus(
            id,
            followUp.memberId,
            followUp.type,
            updateAt,
            followUp.encounterType,
            followUp.reason,
        )
    }

    override suspend fun insertOrUpdateHHFromBE(entity: HouseholdEntity): Long = householdDAO.insertOrUpdateFromBE(entity)

    override suspend fun insertOrUpdateHHMFromBE(entity: HouseholdMemberEntity): Long = memberDAO.insertOrUpdateFromBE(entity)

    override suspend fun changeHouseholdStatus(
        idList: List<String>,
        syncStatus: String,
    ) {
        householdDAO.updateInProgress(idList, syncStatus)
    }

    override suspend fun changeHouseholdMemberStatus(
        idList: List<String>,
        syncStatus: String,
    ) {
        memberDAO.updateInProgress(idList, syncStatus)
    }

    override suspend fun changeAssessmentStatus(
        idList: List<String>,
        syncStatus: String,
    ) {
        assessmentDAO.updateInProgress(idList, syncStatus)
    }

    override suspend fun changeFollowUpStatus(
        idList: List<Long>,
        syncStatus: String,
    ) {
        followUpDao.updateInProgress(idList, syncStatus)
    }

    override suspend fun getPregnancyDetailByPatientId(hhmLocalId: Long): PregnancyDetail? = pregnancyDetailDao.getPregnancyDetailByPatientId(hhmLocalId)

    override suspend fun savePregnancyDetail(detail: PregnancyDetail): Long = pregnancyDetailDao.savePregnancyDetail(detail)

    override suspend fun deleteAllFrequencyList() = frequencyDAO.deleteAllVillages()

    override suspend fun saveFrequencyList(frequencyList: List<FrequencyEntity>): List<Long> = frequencyDAO.insertFrequencyList(frequencyList)

    override suspend fun getFrequencyList(): List<FrequencyEntity> = frequencyDAO.getFrequencyList()

    override suspend fun getInstructionList(): List<MedicalReviewMetaItems> =
        aboveFiveYearsDAO.getSummaryDetailMetaItems(MedicalReviewTypeEnums.PRESCRIPTION_INSTRUCTION.name)

    override fun getExaminationsComplaintsForPnc(
        category: String,
        type: String,
    ): LiveData<List<MedicalReviewMetaItems>> = examinationsComplaintsDAO.getExaminationsComplaintsForPnc(category, type)

    override suspend fun updateOtherFollowUpForWrongNumber(
        id: Long,
        fhirId: String,
    ) {
        followUpDao.updateOtherFollowUpForWrongNumber(id, fhirId)
    }

    override suspend fun insertOrUpdateFollowUp(entity: FollowUp) {
        followUpDao.insertOrUpdateFromBE(entity)
    }

    override suspend fun deleteCompletedFollowUp() {
        followUpDao.deleteCompletedFollowUp()
    }

    override suspend fun getUserHealthFacility(isUserSite: Boolean): ArrayList<HealthFacilityEntity> = ArrayList(metaDataDAO.getUserHealthFacility(isUserSite))

    override suspend fun updateMemberDeceasedStatus(
        id: Long,
        status: Boolean,
    ) {
        memberDAO.updateMemberDeceasedStatus(id, status, OfflineSyncStatus.NotSynced)
        rxBuddyDetailsDAO.updateRxBuddyStatus(id, status)
    }

    override suspend fun saveForm(forms: FormEntity) = metaDataDAO.saveForm(forms)

    override suspend fun saveConsent(consentEntity: ConsentEntity) = metaDataDAO.insertConsent(consentEntity)

    override fun getConsent(formType: String): LiveData<String> = metaDataDAO.getConsent(formType)

    override suspend fun deleteConsent() = metaDataDAO.deleteConsent()

    override suspend fun saveModelQuestions(mentalHealthEntity: List<MentalHealthEntity>) = metaDataDAO.insertModelQuestions(mentalHealthEntity)

    override suspend fun getModelQuestions(formType: String): MentalHealthEntity = metaDataDAO.getModelQuestions(formType)

    override suspend fun deleteModelQuestions() = metaDataDAO.deleteModelQuestions()

    override suspend fun changeFollowUpCallStatus(idList: List<Long>) {
        followUpCallsDao.updateSyncSuccess(idList)
    }

    override suspend fun updateNeonatePatientId(
        hhmLocalId: Long,
        neonateId: Long,
    ) {
        pregnancyDetailDao.updateNeonatePatientId(hhmLocalId, neonateId)
    }

    override suspend fun changeHHMLinkCallStatus(
        idList: List<String>,
        syncStatus: String,
    ) {
        callHistoryDao.updateInProgress(idList, syncStatus)
    }

    override suspend fun changeCommunityProfileStatus(
        idList: List<Long>,
        syncStatus: String,
    ) {
        communityDAO.updateInStatus(idList, syncStatus)
    }

    override suspend fun changeAssignHHMStatus(
        idList: List<String>,
        syncStatus: String,
    ) {
        linkHouseholdMemberDao.updateInProgress(idList, syncStatus)
    }

    override suspend fun getMemberDetailsByPatientId(patientId: String): HouseholdMemberEntity? = memberDAO.getMemberDetailsByPatientId(patientId)

    override suspend fun getChildPatientId(parentId: Long): Long? = pregnancyDetailDao.getChildPatientId(parentId)

    override suspend fun getPatientIdById(id: Long): String = memberDAO.getPatientIdById(id)

    override suspend fun insertConsentForm(form: ConsentForm): Long = consentFormDao.insert(form)

    override suspend fun getConsentFormByType(type: String): ConsentForm? = consentFormDao.getConsentFormByType(type)

    override suspend fun deleteAllConsentForm() {
        consentFormDao.delete()
    }

    override suspend fun getHHSignatureDetails(): List<HHSignatureDetail> = memberDAO.getHHSignatureDetails()

    override suspend fun updatePhoneNumberForHouseholdHead(
        id: Long,
        phoneNumber: String?,
        phoneNumberCategory: String?,
    ) = memberDAO.updatePhoneNumberForHouseholdHead(id, phoneNumber)

    override suspend fun insertLinkHouseholdMembers(insertList: List<LinkHouseholdMember>) {
        linkHouseholdMemberDao.insert(insertList)
    }

    override suspend fun deleteLinkHouseholdMembersById(deleteListIds: List<String>) {
        linkHouseholdMemberDao.delete(deleteListIds)
    }

    override fun getUnAssignedHouseholdMembersLiveData(): LiveData<List<UnAssignedHouseholdMemberDetail>> =
        linkHouseholdMemberDao.getUnAssignedHouseholdMembersLiveData()

    override suspend fun addLinkMemberCall(callHistory: CallHistory): Long = callHistoryDao.insert(callHistory)

    override suspend fun getUnSyncedCallHistoryForHHMLink(): List<HouseholdMemberCallRegisterDto> = callHistoryDao.getUnSyncedCallHistoryForHHMLink()

    override suspend fun changeMemberDetailsToNotSynced(id: Long) {
        memberDAO.changeMemberDetailsToNotSynced(id)
    }

    override suspend fun updateMemberAsAssigned(memberId: String) {
        linkHouseholdMemberDao.updateMemberAsAssigned(memberId)
    }

    override suspend fun saveMedicalCompliance(list: List<MedicalComplianceEntity>) {
        metaDataDAO.insertMedicalCompliance(list)
    }

    override suspend fun getMedicalParentComplianceList(): List<MedicalComplianceEntity> = metaDataDAO.getMedicalComplianceList()

    override suspend fun getMedicalChildComplianceList(parentId: Long): List<MedicalComplianceEntity> = metaDataDAO.getMedicalComplianceList(parentId)

    override suspend fun deleteMedicalCompliance() {
        metaDataDAO.deleteMedicalComplianceList()
    }

    override suspend fun saveDistricts(districts: List<DistrictEntity>) {
        metaDataDAO.insertDistricts(districts)
    }

    override suspend fun getDistricts(countryId: Long): List<DistrictEntity> = metaDataDAO.getDistricts(countryId)

    override suspend fun deleteDistricts() {
        metaDataDAO.deleteCounties()
    }

    override suspend fun saveChiefDoms(chiefdoms: List<ChiefDomEntity>) {
        metaDataDAO.insertChiefDoms(chiefdoms)
    }

    override suspend fun getChiefDoms(districtId: Long): List<ChiefDomEntity> = metaDataDAO.getChiefDoms(districtId)

    override suspend fun deleteChiefDoms() {
        metaDataDAO.deleteChiefDoms()
    }

    override suspend fun savePrograms(programs: List<ProgramEntity>) {
        metaDataDAO.insertPrograms(programs)
    }

    override suspend fun getPrograms(): List<ProgramEntity> = metaDataDAO.getPrograms()

    override suspend fun deletePrograms() {
        metaDataDAO.deletePrograms()
    }

    override fun getMentalQuestion(formType: String): LiveData<MentalHealthEntity?> = metaDataDAO.getMentalQuestion(formType)

    override fun getSites(): LiveData<List<HealthFacilityEntity>> = metaDataDAO.getSites()

    override suspend fun savePatientScreeningInformation(screeningEntity: ScreeningEntity): ScreeningEntity {
        val id = screeningDAO.insertScreening(screeningEntity)
        return screeningDAO.getScreeningById(id)
    }

    override fun getScreenedPatientCount(
        startDate: Long,
        endDate: Long,
        userId: String,
    ): LiveData<Long> = screeningDAO.getScreenedPatientCount(startDate, endDate, userId)

    override fun getScreenedPatientReferredCount(
        startDate: Long,
        endDate: Long,
        userId: String,
        isReferred: Boolean,
    ): LiveData<Long> = screeningDAO.getScreenedPatientReferredCount(startDate, endDate, userId, isReferred)

    override suspend fun getAllScreeningRecords(uploadStatus: Boolean): List<ScreeningEntity> = screeningDAO.getAllScreeningRecords(uploadStatus)

    override suspend fun deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds: Long) =
        screeningDAO.deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds)

    override suspend fun updateScreeningRecordById(
        id: Long,
        uploadStatus: Boolean,
    ) = screeningDAO.updateScreeningRecordById(id, uploadStatus)

    override fun getAssessmentFormData(
        formType: String,
        workFlow: String,
    ): LiveData<String> = metaDataDAO.getAssessmentFormData(formType, workFlow)

    override suspend fun insertRiskFactor(riskFactorEntity: RiskFactorEntity) {
        riskFactorDAO.insertRiskFactor(riskFactorEntity)
    }

    override fun getRiskFactorEntity(): LiveData<List<RiskFactorEntity>> = riskFactorDAO.getAllRiskFactorEntity()

    override suspend fun deleteRiskFactor() = riskFactorDAO.deleteRiskFactor()

    override fun getSymptomListByTypeForNCD(type: String): LiveData<List<SignsAndSymptomsEntity>> = assessmentDAO.getSymptomListByTypeForNCD(type)

    override suspend fun deleteTreatmentPlan() = ncdMedicalReviewDao.deleteTreatmentPlan()

    override suspend fun insertTreatmentPlan(items: List<TreatmentPlanEntity>) = ncdMedicalReviewDao.insertTreatmentPlan(items)

    override suspend fun deleteNCDMedicalReviewMeta() = ncdMedicalReviewDao.deleteNCDMedicalReviewMeta()

    override suspend fun insertNCDMedicalReviewMeta(items: List<NCDMedicalReviewMetaEntity>) = ncdMedicalReviewDao.insertNCDMedicalReviewMeta(items)

    override fun getComorbidities(
        type: String?,
        category: String,
    ): LiveData<List<NCDMedicalReviewMetaEntity>> = ncdMedicalReviewDao.getComorbidities(type, category)

    override suspend fun deleteLifestyle() = ncdMedicalReviewDao.deleteLifestyle()

    override suspend fun insertLifestyle(items: List<LifestyleEntity>) = ncdMedicalReviewDao.insertLifestyle(items)

    override fun getLifeStyle(): LiveData<List<LifestyleEntity>> = ncdMedicalReviewDao.getLifeStyle()

    override fun getAssessmentFormData(
        formTypes: List<String>,
        workFlow: String,
    ): List<String> = metaDataDAO.getAssessmentFormData(formTypes, workFlow)

    override suspend fun getSymptomList(): List<SignsAndSymptomsEntity> = assessmentDAO.getSymptomList()

    override suspend fun saveAssessmentInformation(assessmentOfflineEntity: AssessmentNCDEntity): AssessmentNCDEntity {
        val id = assessmentDAO.saveAssessmentInformation(assessmentOfflineEntity)
        return assessmentDAO.getAssessmentById(id)
    }

    override suspend fun getAllAssessmentRecords(uploadStatus: Boolean): List<AssessmentNCDEntity> = assessmentDAO.getAllAssessmentRecords(uploadStatus)

    override suspend fun updateAssessmentUploadStatus(
        id: Long,
        uploadStatus: Boolean,
    ) = assessmentDAO.updateAssessmentUploadStatus(id, uploadStatus)

    override suspend fun deleteAssessmentList(isUploaded: Boolean) = assessmentDAO.deleteAssessmentList()

    override suspend fun getAssessmentClinicalWorkflow(
        gender: String,
        name: String,
    ): List<NCDAssessmentClinicalWorkflow> = metaDataDAO.getAssessmentClinicalWorkflow(gender, name)

    override fun getUnSyncedDataCountForNCDScreening(): LiveData<Long> = screeningDAO.getUnSyncedDataCountForNCDScreening()

    override fun getUnSyncedNCDAssessmentCount(): LiveData<Long> = assessmentDAO.getUnSyncedNCDAssessmentCount()

    override suspend fun saveNCDDiagnosisList(diseaseEntityList: ArrayList<NCDDiagnosisEntity>) = ncdMedicalReviewDao.saveNCDDiagnosisList(diseaseEntityList)

    override suspend fun deleteNCDDiagnosisList() = ncdMedicalReviewDao.deleteNCDDiagnosisList()

    override fun getNCDDiagnosisList(
        types: List<String>,
        gender: String,
        isPregnant: Boolean,
    ): LiveData<List<NCDDiagnosisEntity>> = ncdMedicalReviewDao.getNCDDiagnosisList(types, gender, isPregnant)

    override fun getFrequencies(): LiveData<List<TreatmentPlanEntity>> = ncdMedicalReviewDao.getFrequencies()

    override suspend fun getNCDShortageReason(type: String): List<ShortageReasonEntity> = ncdMedicalReviewDao.getNCDShortageEntries(type)

    override suspend fun deleteNCDShortageReason() = ncdMedicalReviewDao.deleteNCDShortageReason()

    override suspend fun saveNCDShortageReason(shortageReasonEntity: List<ShortageReasonEntity>) =
        ncdMedicalReviewDao.saveNCDShortageReason(shortageReasonEntity)

    override suspend fun getNCDForm(
        type: String,
        customizedType: String,
    ): List<String> = metaDataDAO.getNCDForm(type, customizedType)

    override suspend fun getUserVillages(): List<VillageEntity> = metaDataDAO.getUserVillages(true)

    override suspend fun deleteDosageDurations() = metaDataDAO.deleteDosageDurations()

    override suspend fun insertDosageDurations(items: List<DosageDurationEntity>) = metaDataDAO.insertDosageDurations(items)

    override suspend fun getDosageDurations(): List<DosageDurationEntity> = metaDataDAO.getDosageDurationsList()

    override suspend fun getUnitList(type: String): List<UnitMetricEntity> = metaDataDAO.getUnitList(type)

    override suspend fun saveUnitMetric(list: ArrayList<UnitMetricEntity>) {
        metaDataDAO.insertUnitMetricList(list)
    }

    override suspend fun getDosageFrequencyList(): List<DosageFrequency> = metaDataDAO.getDosageFrequencyList()

    override suspend fun deleteUnitMetric() {
        metaDataDAO.deleteUnitMetric()
    }

    override suspend fun saveDosageFrequencyList(list: ArrayList<DosageFrequency>) {
        metaDataDAO.insertDosageFrequencyList(list)
    }

    override suspend fun deleteDosageFrequencyList() {
        metaDataDAO.deleteDosageFrequencyList()
    }

    override suspend fun getUnAssignedChildFhirIds(patientId: String): List<HouseholdMemberFhirId> = linkHouseholdMemberDao.getUnAssignedChildFhirIds(patientId)

    override suspend fun getUnAssignedParentFhirId(parentId: String): List<HouseholdMemberFhirId> = linkHouseholdMemberDao.getUnAssignedParentFhirId(parentId)

    override suspend fun updateHouseholdHeadAndRelationShip(
        fhirIds: List<String>,
        householdId: Long,
    ) {
        memberDAO.updateHouseholdHeadAndRelationShip(fhirIds, householdId)
    }

    override suspend fun updateMembersAsAssigned(fhirIds: List<String>) {
        linkHouseholdMemberDao.updateMembersAsAssigned(fhirIds)
    }

    override suspend fun deleteAllNCDFollowUp() {
        ncdFollowUpDao.deleteAllNCDFollowUps()
    }

    override suspend fun insertNCDFollowUp(followUp: NCDFollowUp): Long = ncdFollowUpDao.insertNCDFollowUp(followUp)

    override fun getNCDFollowUpData(
        type: String,
        searchText: String,
        dateBasedOnChip: Pair<Long?, Long?>?,
        isScreened: Boolean?,
        reason: String?,
    ): LiveData<List<NCDFollowUp>> =
        ncdFollowUpDao.getFilteredNCDFollowUp(
            type,
            searchText,
            dateBasedOnChip?.first,
            dateBasedOnChip?.second,
            isScreened,
            reason,
            DateUtils.dateToLong(),
        )

    override suspend fun updatedCallInitiatedCall(ncdFollowUp: NCDFollowUp): NCDFollowUp {
        ncdFollowUpDao.updatedCallInitiated(ncdFollowUp) // Perform the update/insert
        return ncdFollowUpDao.getNCDFollowUpById(ncdFollowUp.id) // Retrieve the updated object
    }

    override suspend fun getNCDInitiatedCallFollowUp(): NCDFollowUp? = ncdFollowUpDao.getNCDInitiatedCallFollowUp()

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

    override suspend fun updateRetryAttempts(
        id: Long,
        retryAttempts: Long,
    ) = ncdFollowUpDao.updateRetryAttempts(id, retryAttempts)

    override suspend fun getAttemptsById(id: Long): Long? = ncdFollowUpDao.getAttemptsById(id)

    override suspend fun getNCDFollowUpById(id: Long): NCDFollowUp = ncdFollowUpDao.getNCDFollowUpById(id)

    override suspend fun getAllNCDCallDetails(): List<NCDCallDetails> = ncdFollowUpDao.getAllNCDCallDetails()

    override suspend fun insertNCDPatientDetails(patients: NCDPatientDetailsEntity): Long = ncdFollowUpDao.insertNCDPatientDetails(patients)

    override suspend fun deleteAllNCDPatientDetails() = ncdFollowUpDao.deleteAllNCDPatientDetails()

    override suspend fun getPatientBasedOnId(id: String): NCDPatientDetailsEntity = ncdFollowUpDao.getPatientBasedOnId(id)

    override suspend fun deleteCallDetails(id: Long) = ncdFollowUpDao.deleteCallDetails(id)

    override fun getUnSyncedNCDFollowUpCount(): LiveData<Long> = ncdFollowUpDao.getUnSyncedNCDFollowUpCount()

    override suspend fun saveCultures(cultures: List<CulturesEntity>) {
        metaDataDAO.insertCultures(cultures)
    }

    override suspend fun getCultures(): List<CulturesEntity> = metaDataDAO.getCultures()

    override suspend fun deleteCultures() {
        metaDataDAO.deleteCultures()
    }

    override suspend fun insertLinkedVillages(linkedVillages: List<LinkedVillageEntity>) {
        metaDataDAO.insertLinkedVillages(linkedVillages)
    }

    override suspend fun deleteAllLinkedVillages() {
        metaDataDAO.deleteAllLinkedVillages()
    }

    override suspend fun updateMemberDeceasedReason(
        id: Long,
        status: Boolean,
        deceasedReason: String?,
    ) {
        memberDAO.updateMemberDeceasedReason(id, status, OfflineSyncStatus.NotSynced, deceasedReason)
        rxBuddyDetailsDAO.updateRxBuddyStatus(id, status)
    }

    override suspend fun getHouseholdHeadDob(householdId: Long): String = memberDAO.getHouseholdHeadDob(householdId)

    override fun getFilterVillagesWithHouseholdsCount(searchInput: String): LiveData<List<CommunityProfileDetail>> =
        metaDataDAO.filterCommunityProfile(searchInput)

    override suspend fun getCommunityStatistics(villageId: Long): CommunityPopulationStatistics = metaDataDAO.getCommunityPopulationStatistics(villageId)

    override suspend fun insertCommunityDetails(communityProfile: CommunityProfile): Long = communityDAO.insertCommunity(communityProfile)

    override suspend fun getCommunityDetails(id: Long): CommunityProfile? = communityDAO.getCommunityDetailsById(id)

    override suspend fun getCommunityProfileId(villageId: Long): Long? = communityDAO.getCommunityProfileId(villageId)

    override suspend fun updateCommunityDetails(communityProfile: CommunityProfile) = communityDAO.updateCommunity(communityProfile)

    override suspend fun updateUnSynStatus(
        villageId: Long,
        synStatus: String,
    ) = communityDAO.updateSyncStatus(villageId, synStatus)

    override suspend fun getHealthFacilityBasedOnVillageId(villageId: Long): List<HealthFacilityEntity> =
        metaDataDAO.getHealthFacilityBasedOnVillageId(villageId)

    override suspend fun getAssessment(assessmentId: Long): AssessmentEntity = assessmentDAO.getAssessment(assessmentId)

    override suspend fun getUnSyncedCommunityDetails(): List<CommunityProfile> = communityDAO.getUnSyncedCommunityDetails()

    override suspend fun insertOrUpdateFromBE(communityProfile: CommunityProfile): Long = communityDAO.insertOrUpdateFromBE(communityProfile)

    override fun householdMemberWithTbStatus(hhId: Long): LiveData<List<HouseholdMemberEntity>> = memberDAO.getHouseholdMemberWithTBContactTraceStatus(hhId)

    override suspend fun updateTBContactTraceStatus(
        hhmId: Long,
        tbContactTracingStatus: Int,
    ) = memberDAO.updateTBContactTraceStatus(hhmId, tbContactTracingStatus)

    override suspend fun updatePregnantStatus(
        memberId: Long,
        isPregnant: Boolean,
    ) = memberDAO.updatePregnantStatus(memberId, isPregnant, syncStatus = OfflineSyncStatus.NotSynced.name)

    override suspend fun getSymptomListByTypes(types: List<String>): List<SignsAndSymptomsEntity> = assessmentDAO.getSymptomListByTypes(types)

    override suspend fun insertRxBuddyDetails(rxBuddyDetails: RxBuddyDetails): Long = rxBuddyDetailsDAO.insertRxBuddyDetails(rxBuddyDetails)

    override suspend fun getRxBuddyDetails(patientMemberId: String): RxBuddyDetails? = rxBuddyDetailsDAO.getRxBuddyDetailsByPatientMemberId(patientMemberId)

    override suspend fun getOtherHouseholdExcludeTBPatient(
        householdId: Long,
        patientId: Long,
    ): List<HouseholdMemberEntity> = memberDAO.getOtherHouseholdExcludeTBPatient(householdId, patientId)

    override suspend fun insertTreatmentDetails(treatmentDetails: TreatmentDetailsEntity): Long = treatmentDetailsDAO.insertTreatmentDetails(treatmentDetails)

    override suspend fun updateTreatmentDetails(treatmentDetails: TreatmentDetailsEntity): Int = treatmentDetailsDAO.updateTreatmentDetails(treatmentDetails)

    override suspend fun getTreatmentDetails(memberId: String): TreatmentDetailsEntity? = treatmentDetailsDAO.getTreatmentDetailsByMemberId(memberId)

    override suspend fun insertRxBuddyFollowUp(rxBuddyFollowUp: RxBuddyFollowUpEntity): Long = rxBuddyFollowUpDAO.insertRxBuddyFollowUp(rxBuddyFollowUp)

    override suspend fun getHivMetaData(): List<MedicalReviewMetaItems> = hivMetaDataDAO.getHivMetaItems()

    override suspend fun getAllUnSyncedRxBuddyRegister(): List<RxBuddyRegisterDetail> = rxBuddyDetailsDAO.getAllUnSyncedRxBuddyRegister()

    override suspend fun getHouseholdMemberForRxBuddy(hhmId: Long): HouseHoldMember = memberDAO.getHouseholdMemberForRxBuddy(hhmId)

    override suspend fun getUnSyncedRxBuddyFollowUpWithoutRxBuddyId(rxBuddyLocalId: Long): List<RxBuddyFollowUpEntity> =
        rxBuddyFollowUpDAO.getUnSyncedRxBuddyFollowUpWithoutRxBuddyId(rxBuddyLocalId)

    override suspend fun getUnSyncedRxBuddyFollowUpWithRxBuddyId(): List<RxBuddyFollowUpDetails> = rxBuddyFollowUpDAO.getUnSyncedRxBuddyFollowUpWithRxBuddyId()

    override suspend fun updateNextVisitDateRxBuddyRegister(
        nextVisitDate: String,
        id: Long,
    ) {
        rxBuddyDetailsDAO.updateNextVisitDate(id, nextVisitDate)
    }

    override suspend fun updateNextVisitDateRxBuddyFollowUp(
        nextVisitDate: String,
        id: Long,
    ) {
        rxBuddyFollowUpDAO.updateNextVisitDate(id, nextVisitDate)
    }

    override suspend fun insertOrUpdateRxBuddyFromBE(entity: RxBuddyDetails): Long = rxBuddyDetailsDAO.insertOrUpdateFromBE(entity)

    override suspend fun deleteAllTreatmentDetails() {
        treatmentDetailsDAO.deleteAllTreatmentDetails()
    }

    override suspend fun deleteAllRxBuddyDetails() {
        rxBuddyDetailsDAO.deleteAllRxBuddyDetails()
    }

    override suspend fun deleteAllRxBuddyFollowUp() {
        rxBuddyFollowUpDAO.deleteAllRxBuddyFollowUp()
    }

    override suspend fun getUnSyncedRxBuddyRegisterCount(): Int = rxBuddyDetailsDAO.getUnSyncedCount()

    override suspend fun getUnSyncedRxBuddyFollowUpCount(): Int = rxBuddyFollowUpDAO.getUnSyncedCount()

    override suspend fun updateRxBuddyRegisterSyncStatus(
        idList: List<Long>,
        syncStatus: String,
    ) {
        rxBuddyDetailsDAO.updateSyncStatus(idList, syncStatus)
    }

    override suspend fun updateRxBuddyFollowUpSyncStatus(
        idList: List<Long>,
        syncStatus: String,
    ) {
        rxBuddyFollowUpDAO.updateSyncStatus(idList, syncStatus)
    }

    override suspend fun deleteDisableRxBuddies(ids: List<Long>) {
        rxBuddyDetailsDAO.deleteAllDisabledRxBuddies(ids)
    }

    override suspend fun getHouseholdMemberIdAndStatusByFhirId(fhirId: String): HouseholdMemberStatus? = memberDAO.getHouseholdMemberIdAndStatusByFhirId(fhirId)

    override suspend fun getUnSyncedHouseHoldByMemberId(hhmId: Long): HouseHold? = householdDAO.getUnSyncedHouseHoldByMemberId(hhmId)

    override suspend fun getHouseholdsWithMemberCountsExceeding(): List<HouseholdWithMemberCount> = memberDAO.getHouseholdsWithMemberCountsExceeding()

    override suspend fun getMemberFhirIdByLocalId(hhmId: Long): String? = memberDAO.getMemberFhirIdByLocalId(hhmId)

    override suspend fun getAllUnSyncedRxBuddyDetailWithHHM(
        hhmId: Long,
        rxBuddiesId: List<Long>,
    ): List<RxBuddyRegisterDetail> = rxBuddyDetailsDAO.getAllUnSyncedRxBuddyDetailWithHHM(hhmId, rxBuddiesId)

    override suspend fun getTbPatientLocalIdByHouseholdId(householdId: Long): MutableList<Long> = memberDAO.getTbPatientLocalIdByHouseholdId(householdId)

    override suspend fun updateContactTracingStatus(
        memberId: Long,
        status: Int?,
    ) {
        memberDAO.updateContactTracingStatus(memberId, status)
    }

    override suspend fun updateContactTracingForLinkTbPatient(
        tbHHMId: Long,
        householdId: Long,
    ) {
        memberDAO.updateContactTracingForLinkTbPatient(tbHHMId, householdId)
    }
}
