package com.medtroniclabs.spice.network

import com.google.gson.JsonObject
import com.medtroniclabs.spice.data.HivVitalDetailsRequest
import com.medtroniclabs.spice.data.HivVitalDetailsResponse
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.AboveFiveYearsMetaResponse
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.BirthHistoryRequest
import com.medtroniclabs.spice.data.BirthHistoryResponse
import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.DiagnosisSaveUpdateRequest
import com.medtroniclabs.spice.data.DispensePrescriptionRequest
import com.medtroniclabs.spice.data.DispensePrescriptionResponse
import com.medtroniclabs.spice.data.DispenseUpdateRequest
import com.medtroniclabs.spice.data.DispenseUpdateResponse
import com.medtroniclabs.spice.data.FamilyPlanningMetaResponse
import com.medtroniclabs.spice.data.FormMetaRequest
import com.medtroniclabs.spice.data.FormRequest
import com.medtroniclabs.spice.data.FormResponse
import com.medtroniclabs.spice.data.HivClinicalInfoResponse
import com.medtroniclabs.spice.data.LabourDeliveryMetaResponse
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.MedicalReviewSummarySubmitRequest
import com.medtroniclabs.spice.data.MedicationGroupSearchRequest
import com.medtroniclabs.spice.data.MedicationResponse
import com.medtroniclabs.spice.data.MedicationSearchRequest
import com.medtroniclabs.spice.data.MetaDataResponse
import com.medtroniclabs.spice.data.MotherNeonateAncMetaResponse
import com.medtroniclabs.spice.data.MotherNeonateAncSummaryModel
import com.medtroniclabs.spice.data.MotherNeonatePncSummaryRequest
import com.medtroniclabs.spice.data.MotherNeonatePncSummaryResponse
import com.medtroniclabs.spice.data.MotherPncResponse
import com.medtroniclabs.spice.data.NCDUserDashboardRequest
import com.medtroniclabs.spice.data.NCDUserDashboardResponse
import com.medtroniclabs.spice.data.NeonatePncResponse
import com.medtroniclabs.spice.data.PatientStatusRequest
import com.medtroniclabs.spice.data.PatientStatusResponse
import com.medtroniclabs.spice.data.PncChildMedicalReview
import com.medtroniclabs.spice.data.PregnancyDetailsModel
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.data.PrescriptionListRequest
import com.medtroniclabs.spice.data.ReferPatientAPIRequest
import com.medtroniclabs.spice.data.ReferPatientHealthFacilityItem
import com.medtroniclabs.spice.data.ReferPatientNameNumber
import com.medtroniclabs.spice.data.ReferPatientRequest
import com.medtroniclabs.spice.data.ReferPatientResult
import com.medtroniclabs.spice.data.RemovePrescriptionRequest
import com.medtroniclabs.spice.data.SummaryCreateRequest
import com.medtroniclabs.spice.data.TbMetaResponse
import com.medtroniclabs.spice.data.UnderFiveYearsMetaResponse
import com.medtroniclabs.spice.data.UnderTwoMonthsMetaResponse
import com.medtroniclabs.spice.data.UserSymptomsEntity
import com.medtroniclabs.spice.data.WhoClinicalStageCreateRequest
import com.medtroniclabs.spice.data.history.BirthDetails
import com.medtroniclabs.spice.data.history.HistoryEntity
import com.medtroniclabs.spice.data.history.MedicalReviewHistory
import com.medtroniclabs.spice.data.history.NCDMedicalReviewHistory
import com.medtroniclabs.spice.data.model.AboveFiveYearsSubmitRequest
import com.medtroniclabs.spice.data.model.BpAndWeightRequestModel
import com.medtroniclabs.spice.data.model.BpAndWeightResponse
import com.medtroniclabs.spice.data.model.CreateLabourDeliveryRequest
import com.medtroniclabs.spice.data.model.CreateLabourDeliveryResponse
import com.medtroniclabs.spice.data.model.FamilyPlanningContraceptivesRequest
import com.medtroniclabs.spice.data.model.FamilyPlanningCreateResponse
import com.medtroniclabs.spice.data.model.FamilyPlanningSummaryResponse
import com.medtroniclabs.spice.data.model.HivCreateScreeningSummaryResponse
import com.medtroniclabs.spice.data.model.HivMedicalReviewSummaryRequest
import com.medtroniclabs.spice.data.model.HivMedicalReviewSummaryResponse
import com.medtroniclabs.spice.data.model.HivMetaResponse
import com.medtroniclabs.spice.data.model.HivRequestData
import com.medtroniclabs.spice.data.model.HivScreeningRequest
import com.medtroniclabs.spice.data.model.HivScreeningResponse
import com.medtroniclabs.spice.data.model.HivSummaryResponse
import com.medtroniclabs.spice.data.model.LabourDeliverySummaryDetails
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.MotherNeonatePncRequest
import com.medtroniclabs.spice.data.model.PatientEncounterResponse
import com.medtroniclabs.spice.data.model.PatientTypeCreateRequest
import com.medtroniclabs.spice.data.model.PncSubmitResponse
import com.medtroniclabs.spice.data.model.RegistrationResponse
import com.medtroniclabs.spice.data.model.RequestChangePassword
import com.medtroniclabs.spice.data.model.ResponseChangePassword
import com.medtroniclabs.spice.data.model.TbHistory
import com.medtroniclabs.spice.data.model.TbMedicalReviewCreateRequest
import com.medtroniclabs.spice.data.model.ViralLoadRequest
import com.medtroniclabs.spice.data.model.ViralLoadResponse
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.model.ResponseSignatureUpload
import com.medtroniclabs.spice.data.offlinesync.model.SyncResponse
import com.medtroniclabs.spice.data.performance.CHWPerformanceMonitoring
import com.medtroniclabs.spice.data.performance.ChwVillageFilterModel
import com.medtroniclabs.spice.data.performance.FilterPreference
import com.medtroniclabs.spice.data.performance.PerformanceReportRequest
import com.medtroniclabs.spice.data.resource.CD4DetailsRequest
import com.medtroniclabs.spice.data.resource.CD4DetailsResponse
import com.medtroniclabs.spice.data.resource.LabourDeliverySummaryRequest
import com.medtroniclabs.spice.data.resource.RequestAllEntities
import com.medtroniclabs.spice.model.ARTResponse
import com.medtroniclabs.spice.model.ArtRequest
import com.medtroniclabs.spice.model.CultureLocaleModel
import com.medtroniclabs.spice.model.LabTestCreateRequest
import com.medtroniclabs.spice.model.LabTestListRequest
import com.medtroniclabs.spice.model.LabTestListResponse
import com.medtroniclabs.spice.model.NcdMRStaticDataModel
import com.medtroniclabs.spice.model.PatientDetailRequest
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.PatientsDataModel
import com.medtroniclabs.spice.model.PregnancySummaryRequest
import com.medtroniclabs.spice.model.ReferralData
import com.medtroniclabs.spice.model.ReferralDetailRequest
import com.medtroniclabs.spice.model.RemoveLabTestRequest
import com.medtroniclabs.spice.model.SearchAndListResponse
import com.medtroniclabs.spice.model.communityprofile.CommunityProfileDetails
import com.medtroniclabs.spice.model.medicalreview.AddMemberRegRequest
import com.medtroniclabs.spice.model.medicalreview.CreateUnderFiveYearsRequest
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsRequest
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.EMTCTVisitStatusRequest
import com.medtroniclabs.spice.model.medicalreview.EMTCTVisitStatusResponse
import com.medtroniclabs.spice.model.medicalreview.HivVitalsRequest
import com.medtroniclabs.spice.model.medicalreview.HivVitalsResponse
import com.medtroniclabs.spice.model.medicalreview.RequestBirthDetails
import com.medtroniclabs.spice.model.medicalreview.RequestCreateImmunisation
import com.medtroniclabs.spice.model.medicalreview.RequestImmunisationSummaryCreate
import com.medtroniclabs.spice.model.medicalreview.RequestImmunisationSummaryDetail
import com.medtroniclabs.spice.model.medicalreview.RequestVaccinationList
import com.medtroniclabs.spice.model.medicalreview.ResponseCreateImmunisation
import com.medtroniclabs.spice.model.medicalreview.ResponseImmunisationSummaryCreate
import com.medtroniclabs.spice.model.medicalreview.ResponseImmunisationSummaryDetails
import com.medtroniclabs.spice.model.medicalreview.SearchLabTestResponse
import com.medtroniclabs.spice.model.medicalreview.SearchRequestLabTest
import com.medtroniclabs.spice.model.medicalreview.SummaryDetails
import com.medtroniclabs.spice.model.medicalreview.VaccinationDetail
import com.medtroniclabs.spice.model.medicalreview.WazWhzScoreRequest
import com.medtroniclabs.spice.model.medicalreview.WazWhzScoreResponse
import com.medtroniclabs.spice.ncd.data.AssessmentResultModel
import com.medtroniclabs.spice.ncd.data.BPBGListModel
import com.medtroniclabs.spice.ncd.data.BadgeNotificationModel
import com.medtroniclabs.spice.ncd.data.DeviceDetails
import com.medtroniclabs.spice.ncd.data.FollowUpRequest
import com.medtroniclabs.spice.ncd.data.FollowUpUpdateRequest
import com.medtroniclabs.spice.ncd.data.LifeStyleRequest
import com.medtroniclabs.spice.ncd.data.LifeStyleResponse
import com.medtroniclabs.spice.ncd.data.MRSummaryResponse
import com.medtroniclabs.spice.ncd.data.MedicalReviewRequestResponse
import com.medtroniclabs.spice.ncd.data.MedicalReviewResponse
import com.medtroniclabs.spice.ncd.data.NCDCounselingModel
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisGetRequest
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisGetResponse
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisRequestResponse
import com.medtroniclabs.spice.ncd.data.NCDInstructionModel
import com.medtroniclabs.spice.ncd.data.NCDMRSummaryRequestResponse
import com.medtroniclabs.spice.ncd.data.NCDMedicalReviewUpdateModel
import com.medtroniclabs.spice.ncd.data.NCDMentalHealthMedicalReviewDetails
import com.medtroniclabs.spice.ncd.data.NCDMentalHealthStatusRequest
import com.medtroniclabs.spice.ncd.data.NCDPatientRemoveRequest
import com.medtroniclabs.spice.ncd.data.NCDPatientStatusRequest
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferNotificationCountRequest
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferNotificationCountResponse
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferUpdateRequest
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferValidate
import com.medtroniclabs.spice.ncd.data.NCDPregnancyRiskUpdate
import com.medtroniclabs.spice.ncd.data.NCDRegionSiteModel
import com.medtroniclabs.spice.ncd.data.NCDSiteRoleModel
import com.medtroniclabs.spice.ncd.data.NCDSiteRoleResponse
import com.medtroniclabs.spice.ncd.data.NCDSupportRequest
import com.medtroniclabs.spice.ncd.data.NCDTransferCreateRequest
import com.medtroniclabs.spice.ncd.data.NCDTreatmentPlanModel
import com.medtroniclabs.spice.ncd.data.NCDTreatmentPlanModelDetails
import com.medtroniclabs.spice.ncd.data.PatientFollowUpEntity
import com.medtroniclabs.spice.ncd.data.PatientTransferListResponse
import com.medtroniclabs.spice.ncd.data.PatientVisitRequest
import com.medtroniclabs.spice.ncd.data.PatientVisitResponse
import com.medtroniclabs.spice.ncd.data.PeerSupervisorNotificationRequest
import com.medtroniclabs.spice.ncd.data.PeerSupervisorNotificationResponse
import com.medtroniclabs.spice.ncd.data.PredictionRequest
import com.medtroniclabs.spice.ncd.data.PrescriptionNudgeResponse
import com.medtroniclabs.spice.ncd.data.RegionSiteResponse
import com.medtroniclabs.spice.ncd.data.RegisterCallResponse
import com.medtroniclabs.spice.ncd.data.TermsAndConditionsModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(private val apiService: ApiService) : ApiHelper {
    override suspend fun doLogin(loginRequest: MultipartBody): Response<LoginResponse> {
        return apiService.doLogin(loginRequest)
    }

    override suspend fun getMetaDataInformation(): Response<APIResponse<MetaDataResponse>> {
        return apiService.getMetaDataInformation()
    }

    override suspend fun getForms(formRequest: FormRequest): Response<APIResponse<FormResponse>> {
        return apiService.getForms(formRequest)
    }

    override suspend fun getFormMetadata(request: FormMetaRequest): Response<APIResponse<UserSymptomsEntity>> {
        return apiService.getFormMetadata(request)
    }

    override suspend fun getPatients(request: PatientsDataModel): APIResponse<SearchAndListResponse> {
        return apiService.getPatients(request)
    }

    override suspend fun postOfflineSync(request: Map<String, Any>): Response<SyncResponse> {
        return apiService.postOfflineSyncData(request)
    }

    override suspend fun getOfflineSyncStatus(request: RequestGetSyncStatus): Response<SyncResponse> {
        return apiService.getOfflineSyncStatus(request)
    }

    override suspend fun fetchSyncedData(request: RequestAllEntities): Response<ResponseBody> {
        return apiService.fetchSyncedData(request)
    }

    override suspend fun getHouseholdAndMembers(request: RequestAllEntities): Response<APIResponse<List<HouseHold>>> {
        return apiService.getHouseholdDetails(request)
    }

    override suspend fun patientSearch(request: PatientsDataModel): APIResponse<SearchAndListResponse> {
        return apiService.patientSearch(request)
    }

    override suspend fun getPatient(request: PatientDetailRequest): Response<APIResponse<PatientListRespModel>> {
        return apiService.getPatient(request)
    }

    override suspend fun getAboveFiveYearsMetaData(): Response<APIResponse<AboveFiveYearsMetaResponse>> {
        return apiService.getAboveFiveYearsMetaData()
    }

    override suspend fun getReferralsDetails(request: ReferralDetailRequest): Response<APIResponse<ReferralData>> {
        return apiService.getReferralsDetails(request)
    }

    override suspend fun createAboveFiveYearsResult(request: AboveFiveYearsSubmitRequest): Response<APIResponse<AboveFiveYearsSummaryDetails>> {
        return apiService.createAboveFiveYearsResult(request)
    }

    override suspend fun getAboveFiveYearsSummaryDetails(patientId: AboveFiveYearsSummaryRequest): Response<APIResponse<AboveFiveYearsSummaryDetails>> {
        return apiService.getAboveFiveYearsSummaryDetails(patientId)
    }

    override suspend fun getMotherNeoNateAncStaticData(): Response<APIResponse<MotherNeonateAncMetaResponse>> {
        return apiService.getMotherNeoNateAncStaticData()
    }

    override suspend fun getMotherPncStaticData(): Response<APIResponse<MotherPncResponse>> {
        return apiService.getMotherPncStaticData()
    }
    override suspend fun getNeonatePncStaticData(): Response<APIResponse<NeonatePncResponse>> {
        return apiService.getNeonatePncStaticData()
    }

    override suspend fun getUnderTwoMonthsMetaData(): Response<APIResponse<UnderTwoMonthsMetaResponse>> {
        return apiService.getUnderTwoMonthsMetaData()
    }

    override suspend fun createSummarySubmit(request: MedicalReviewSummarySubmitRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createSummarySubmit(request)
    }

    override suspend fun getLabourDeliveryMetaData(): Response<APIResponse<LabourDeliveryMetaResponse>> {
        return apiService.getLabourDeliveryMetaData()
    }
    override suspend fun getLabourDeliverySummaryDetails(request: LabourDeliverySummaryDetails): Response<APIResponse<CreateLabourDeliveryRequest>> {
        return apiService.getLabourDeliverySummaryDetails(request)
    }

    override suspend fun getPatientStatus(request: PatientStatusRequest): Response<APIResponse<PatientStatusResponse>> {
        return apiService.getPatientStatus(request)
    }

    override suspend fun searchMedicationByName(request: MedicationSearchRequest): Response<APIResponse<ArrayList<MedicationResponse>>> {
        return apiService.searchMedicationByName(request)
    }

    override suspend fun searchMedicationGroupByName(request: MedicationGroupSearchRequest): Response<APIResponse<ArrayList<MedicationResponse>>> {
        return apiService.searchMedicationGroupByName(request)
    }

    override suspend fun createMedicalReviewForUnderTwoMonths(request: CreateUnderTwoMonthsRequest): Response<APIResponse<CreateUnderTwoMonthsResponse>> {
        return apiService.createMedicalReviewForUnderTwoMonths(request)
    }

    override suspend fun getMedicalReviewForUnderTwoMonths(request: CreateUnderTwoMonthsResponse): Response<APIResponse<SummaryDetails>> {
        return apiService.getUnderTwoMonthsSummaryDetails(request)
    }


    override suspend fun saveMotherNeonateAnc(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<PatientEncounterResponse>> {
        return apiService.saveMotherNeonateAnc(motherNeonateAncRequest)
    }
    override suspend fun saveMotherNeonatePnc(motherNeonatePncRequest: MotherNeonatePncRequest): Response<APIResponse<PncSubmitResponse>> {
        return apiService.saveMotherNeonatePnc(motherNeonatePncRequest)
    }


    override suspend fun fetchSummary(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<MotherNeonateAncSummaryModel>> {
        return apiService.fetchSummary(motherNeonateAncRequest)
    }

    override suspend fun fetchWeight(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<BpAndWeightResponse>> {
        return apiService.fetchWeight(motherNeonateAncRequest)
    }

    override suspend fun fetchBloodPressure(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<BpAndWeightResponse>> {
        return apiService.fetchBloodPressure(motherNeonateAncRequest)
    }

    override suspend fun createWeight(bpAndWeightRequestModel: BpAndWeightRequestModel): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createWeight(bpAndWeightRequestModel)
    }

    override suspend fun createBloodPressure(bpAndWeightRequestModel: BpAndWeightRequestModel): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createBloodPressure(bpAndWeightRequestModel)
    }

    override suspend fun saveUpdateDiagnosis(request: DiagnosisSaveUpdateRequest): Response<APIResponse<ArrayList<DiagnosisDiseaseModel>>> {
        return apiService.saveUpdateDiagnosis(request)
    }

    override suspend fun getDiagnosisDetails(request: CreateUnderTwoMonthsResponse): Response<APIResponse<ArrayList<DiagnosisDiseaseModel>>> {
        return apiService.getDiagnosisDetails(request)
    }

    override suspend fun getHealthFacilityMetaData(request: ReferPatientAPIRequest): Response<APIResponse<List<ReferPatientHealthFacilityItem>>> {
        return apiService.getHealthFacilityMetaData(request)
    }

    override suspend fun getReferPatientMobileUserList(tenantId: ReferPatientRequest): Response<APIResponse<List<ReferPatientNameNumber>>> {
        return apiService.getReferPatientMobileUserList(tenantId)
    }

    override suspend fun createReferPatientResult(request: ReferPatientResult): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createReferPatientResult(request)
    }

    override suspend fun createPrescriptionRequest(request: RequestBody): Response<APIResponse<Map<String, Any>>> {
        return apiService.createPrescriptionRequest(request)
    }

    override suspend fun getPrescriptionList(request: PrescriptionListRequest): Response<APIResponse<ArrayList<Prescription>>> {
        return apiService.getPrescriptionList(request)
    }

    override suspend fun removePrescription(request: RemovePrescriptionRequest): Response<APIResponse<Map<String, Any>>> {
        return apiService.removePrescription(request)
    }

    override suspend fun removeCommunityPrescription(request: List<RemovePrescriptionRequest>): Response<APIResponse<Map<String, Any>>> {
        return apiService.removeCommunityPrescription(request)
    }

    override suspend fun getUnderFiveYearsMetaData(): Response<APIResponse<UnderFiveYearsMetaResponse>> {
        return apiService.getUnderFiveYearsMetaData()
    }

    override suspend fun createMedicalReviewForUnderFiveYears(request: CreateUnderFiveYearsRequest): Response<APIResponse<CreateUnderTwoMonthsResponse>> {
        return apiService.createMedicalReviewForUnderFiveYears(request)
    }

    override suspend fun getUnderFiveYearsSummaryDetails(request: CreateUnderTwoMonthsResponse): Response<APIResponse<SummaryDetails>> {
        return apiService.getUnderFiveYearsSummaryDetails(request)
    }


    override suspend fun getPrescription(request: ReferralDetailRequest): Response<APIResponse<HistoryEntity>> {
        return apiService.getPrescription(request)
    }

    override suspend fun getMedicalReviewHistory(request: ReferralDetailRequest): Response<APIResponse<MedicalReviewHistory>> {
        return apiService.getMedicalReviewHistory(request)
    }

    override suspend fun getMedicalReviewHistoryPNC(request: ReferralDetailRequest): Response<APIResponse<PncChildMedicalReview>> {
        return apiService.getMedicalReviewHistoryPNC(request)
    }

    override suspend fun getPncSummaryDetails(request: MotherNeonatePncSummaryRequest): Response<APIResponse<MotherNeonatePncSummaryResponse>> {
        return apiService.getPncSummaryDetails(request)
    }

    override suspend fun summaryCreatePncData(summaryCreateRequest: SummaryCreateRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.summaryCreatePncData(summaryCreateRequest)
    }
    override suspend fun getBirthHistoryDetails(request: BirthHistoryRequest): Response<APIResponse<BirthHistoryResponse>> {
        return apiService.getBirthHistoryDetails(request)
    }

    override suspend fun createMedicalReviewLabourDelivery(request: CreateLabourDeliveryRequest): Response<APIResponse<CreateLabourDeliveryResponse>> {
        return apiService.createMedicalReviewForLaborDelivery(request)
    }

    override suspend fun addNewMember(request: AddMemberRegRequest): Response<APIResponse<String>> {
        return apiService.addNewMember(request)
    }

    override suspend fun searchLabTestByName(request: SearchRequestLabTest): Response<APIResponse<ArrayList<SearchLabTestResponse>>> {
        return apiService.searchLabTestByName(request)
    }

    override suspend fun createLabTest(request: LabTestCreateRequest): Response<APIResponse<Map<String, Any>>> {
        return apiService.createLabTest(request)
    }

    override suspend fun updateLabTest(request: LabTestCreateRequest): Response<APIResponse<Map<String, Any>>> {
        return apiService.updateLabTest(request)
    }

    override suspend fun getLabTestList(request: LabTestListRequest): Response<APIResponse<ArrayList<LabTestListResponse>>> {
        return apiService.getLabTestList(request)
    }

    override suspend fun removeLabTest(request: RemoveLabTestRequest): Response<APIResponse<Map<String, Any>>> {
        return apiService.removeLabTest(request)
    }
    override suspend fun createSummaryMotherNeonate(request: LabourDeliverySummaryRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.summaryCreateMotherNeonate(request)
    }

    override suspend fun getPeerSupervisorLinkedChwList(): Response<APIResponse<List<ChwVillageFilterModel>>> {
        return apiService.getPeerSupervisorLinkedChwList()
    }

    override suspend fun getPeerSupervisorReport(request: PerformanceReportRequest): Response<APIResponse<List<CHWPerformanceMonitoring>>> {
        return apiService.getPeerSupervisorReport(request)
    }

    override suspend fun getUserFilterPreference(request: FilterPreference): Response<APIResponse<FilterPreference>> {
        return apiService.getUserFilterPreference(request)
    }

    override suspend fun saveUserFilterPreference(request: FilterPreference): Response<APIResponse<FilterPreference>> {
        return apiService.saveUserFilterPreference(request)
    }

    override suspend fun getInvestigation(request: ReferralDetailRequest): Response<APIResponse<HistoryEntity>> {
        return apiService.getInvestigationHistoryList(request)
    }

    override suspend fun forgotPassword(email: String, clientConstant: String): Response<APIResponse<String?>> {
        return apiService.forgotPassword(email, clientConstant)
    }

    override suspend fun verifyToken(token: String): Response<APIResponse<String?>> {
        return apiService.verifyToken(token)
    }

    override suspend fun resetPassword(token: String, request: RequestChangePassword): Response<APIResponse<ResponseChangePassword>> {
        return apiService.resetPassword(token, request)
    }

    override suspend fun uploadAllConsentSignatures(request: RequestBody): Response<APIResponse<List<ResponseSignatureUpload>>> {
        return apiService.uploadAllConsentSignatures(request)
    }

    override suspend fun checkAppVersion(): Response<APIResponse<Boolean>> {
        return apiService.checkAppVersion()
    }

    override suspend fun registerPatient(hashMap: RequestBody): Response<APIResponse<RegistrationResponse>> {
        return apiService.registerPatient(hashMap)
    }

    override suspend fun createScreening(createRequest: RequestBody): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createScreening(createRequest)
    }

    override suspend fun getNcdMRStaticData(): Response<APIResponse<NcdMRStaticDataModel>> {
        return apiService.getNcdMRStaticData()
    }

    override suspend fun bpLogCreate(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.bpLogCreate(request)
    }

    override suspend fun glucoseLogCreate(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.glucoseLogCreate(request)
    }

    override suspend fun bpLogList(request: BPBGListModel): Response<APIResponse<BPBGListModel>> {
        return apiService.bpLogList(request)
    }

    override suspend fun glucoseLogList(request: BPBGListModel): Response<APIResponse<BPBGListModel>> {
        return apiService.glucoseLogList(request)
    }
    override suspend fun createAssessmentNCD(request: JsonObject): Response<HashMap<String, Any>> {
        return apiService.createAssessmentNCD(request)
    }

    override suspend fun ncdPregnancyCreate(request: PregnancyDetailsModel): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.ncdPregnancyCreate(request)
    }

    override suspend fun ncdPregnancyDetails(request: HashMap<String, Any>): Response<APIResponse<PregnancyDetailsModel>> {
        return apiService.ncdPregnancyDetails(request)
    }

    override suspend fun createPatientVisit(request: PatientVisitRequest): Response<APIResponse<PatientVisitResponse>> {
        return apiService.createPatientVisit(request)
    }

    override suspend fun createNCDMedicalReview(request: MedicalReviewRequestResponse): Response<APIResponse<MedicalReviewResponse>> {
        return apiService.createNCDMedicalReview(request)
    }

    override suspend fun fetchNCDMRSummary(request: MedicalReviewResponse): Response<APIResponse<MRSummaryResponse>> {
        return apiService.fetchNCDMRSummary(request)
    }

    override suspend fun getPatientPrescriptionHistoryList(request: RemovePrescriptionRequest): Response<APIResponse<ArrayList<Prescription>>> {
        return apiService.getPatientPrescriptionHistoryList(request)
    }

    override suspend fun createConfirmDiagonsis(request: NCDDiagnosisRequestResponse): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createConfirmDiagonsis(request)
    }

    override suspend fun getConfirmDiagonsis(request: NCDDiagnosisGetRequest): Response<APIResponse<NCDDiagnosisGetResponse>> {
        return apiService.getConfirmDiagonsis(request)
    }

    override suspend fun createNCDPatientStatus(request: NCDPatientStatusRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createNCDPatientStatus(request)
    }

    override suspend fun updateNCDTreatmentPlan(request: NCDTreatmentPlanModel): Response<APIResponse<NCDTreatmentPlanModel>> {
        return apiService.updateNCDTreatmentPlan(request)
    }

    override suspend fun getNCDTreatmentPlan(request: NCDTreatmentPlanModelDetails): Response<APIResponse<NCDTreatmentPlanModelDetails>> {
        return apiService.getNCDTreatmentPlan(request)
    }

    override suspend fun createNCDMRSummaryCreate(request: NCDMRSummaryRequestResponse): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createNCDMRSummaryCreate(request)
    }

    override suspend fun getPrescriptionDispenseList(request: DispenseUpdateRequest): Response<APIResponse<ArrayList<DispensePrescriptionResponse>>> {
        return apiService.getPrescriptionDispenseList(request)
    }

    override suspend fun updateDispensePrescription(request: DispensePrescriptionRequest): Response<APIResponse<DispenseUpdateResponse>> {
        return apiService.updateDispensePrescription(request)
    }

    override suspend fun getDispensePrescriptionHistory(request: DispenseUpdateRequest): Response<APIResponse<ArrayList<DispensePrescriptionResponse>>> {
        return apiService.getDispensePrescriptionHistory(request)
    }

    override suspend fun createLifestyle(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>> {
        return apiService.createLifestyle(request)
    }

    override suspend fun updateLifestyle(request: AssessmentResultModel): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.updateLifestyle(request)
    }

    override suspend fun getLifestyleList(request: NCDCounselingModel): Response<APIResponse<ArrayList<NCDCounselingModel>>> {
        return apiService.getLifestyleList(request)
    }

    override suspend fun removeLifestyle(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>> {
        return apiService.removeLifestyle(request)
    }

    override suspend fun createPsychological(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>> {
        return apiService.createPsychological(request)
    }

    override suspend fun updatePsychological(request: AssessmentResultModel): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.updatePsychological(request)
    }

    override suspend fun getPsychological(request: NCDCounselingModel): Response<APIResponse<ArrayList<NCDCounselingModel>>> {
        return apiService.getPsychological(request)
    }

    override suspend fun removePsychological(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>> {
        return apiService.removePsychological(request)
    }

    override suspend fun getNCDMedicalReviewHistory(request: ReferralDetailRequest): Response<APIResponse<NCDMedicalReviewHistory>> {
        return apiService.getNCDMedicalReviewHistory(request)
    }

    override suspend fun validatePatient(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.validatePatient(request)
    }

    override suspend fun ncdGetInstructions(): Response<APIResponse<NCDInstructionModel>> {
        return apiService.ncdGetInstructions()
    }

    override suspend fun ncdUpdatePregnancyRisk(request: NCDPregnancyRiskUpdate): Response<APIResponse<Boolean>> {
        return apiService.ncdUpdatePregnancyRisk(request)
    }

    override suspend fun getWazWhzScore(request: WazWhzScoreRequest): Response<APIResponse<WazWhzScoreResponse>> {
        return apiService.getWazWhzScore(request)
    }

    override suspend fun getUserDashboardDetails(request: NCDUserDashboardRequest): Response<APIResponse<NCDUserDashboardResponse>> {
        return apiService.getUserDashboardDetails(request)
    }

    override suspend fun getBadgeNotifications(request: BadgeNotificationModel): Response<APIResponse<BadgeNotificationModel>> {
        return apiService.getBadgeNotifications(request)
    }

    override suspend fun updateBadgeNotifications(request: BadgeNotificationModel): Response<APIResponse<Boolean>> {
        return apiService.updateBadgeNotifications(request)
    }

    override suspend fun getNcdLifeStyleDetails(request: LifeStyleRequest): Response<APIResponse<ArrayList<LifeStyleResponse>>> {
        return apiService.getNcdLifeStyleDetails(request)
    }

    override suspend fun ncdPatientRemove(request: NCDPatientRemoveRequest): Response<APIResponse<Boolean>> {
        return apiService.ncdPatientRemove(request)
    }

    override suspend fun bpLogCreateForNurse(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.bpLogCreateForNurse(request)
    }

    override suspend fun glucoseLogCreateForNurse(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.glucoseLogCreateForNurse(request)
    }

    override suspend fun ncdUpdatePatientDetail(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return  apiService.ncdUpdatePatientDetail(request)
    }

    override suspend fun getUserTermsAndConditions(request: TermsAndConditionsModel): Response<APIResponse<TermsAndConditionsModel>> {
        return apiService.getUserTermsAndConditions(request)
    }

    override suspend fun updateTermsAndConditionsStatus(request: TermsAndConditionsModel): Response<APIResponse<TermsAndConditionsModel>> {
        return apiService.updateTermsAndConditionsStatus(request)
    }

    override suspend fun ncdUpdateNextVisitDate(request: NCDMedicalReviewUpdateModel): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.ncdUpdateNextVisitDate(request)
    }

    override suspend fun validatePatientTransfer(request: NCDPatientTransferValidate): Response<HashMap<String, Any>> {
        return apiService.validatePatientTransfer(request)
    }

    override suspend fun createPatientTransfer(request: NCDTransferCreateRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createPatientTransfer(request)
    }

    override suspend fun searchSite(request: NCDRegionSiteModel): Response<APIResponse<ArrayList<RegionSiteResponse>>> {
        return apiService.searchSite(request)
    }

    override suspend fun searchRoleUser(request: NCDSiteRoleModel): Response<APIResponse<ArrayList<NCDSiteRoleResponse>>> {
        return apiService.searchRoleUser(request)
    }

    override suspend fun getPatientListTransfer(request: NCDPatientTransferNotificationCountRequest): Response<APIResponse<PatientTransferListResponse>> {
        return apiService.getPatientListTransfer(request)
    }

    override suspend fun patientTransferNotificationCount(request: NCDPatientTransferNotificationCountRequest): Response<APIResponse<NCDPatientTransferNotificationCountResponse>> {
        return apiService.patientTransferNotificationCount(request)
    }

    override suspend fun patientTransferUpdate(request: NCDPatientTransferUpdateRequest): Response<APIResponse<String>> {
        return apiService.patientTransferUpdate(request)
    }

    override suspend fun getNudgesList(prescriptionNudgeRequest: PredictionRequest): Response<APIResponse<PrescriptionNudgeResponse>> {
        return apiService.getNudgesList(prescriptionNudgeRequest)
    }

    override suspend fun getLabTestNudgeList(predictionRequest: PredictionRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.getLabTestNudgeList(predictionRequest)
    }

    override suspend fun ncdFollowUpList(request: FollowUpRequest): APIResponse<List<PatientFollowUpEntity>> {
        return apiService.ncdFollowUpList(request)
    }

    override suspend fun getPatientCallRegister(): Response<APIResponse<RegisterCallResponse>> {
        return apiService.getPatientCallRegister()
    }

    override suspend fun updatePatientCallRegister(request: FollowUpUpdateRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.updatePatientCallRegister(request)
    }

    override suspend fun updateDeviceDetails(request: DeviceDetails): Response<APIResponse<DeviceDetails>> {
        return apiService.updateDeviceDetails(request)
    }

    override suspend fun createMentalHealthStatus(request: NCDMentalHealthStatusRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createMentalHealthStatus(request)
    }

    override suspend fun ncdMentalHealthMedicalReviewCreateA(request: JsonObject): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.ncdMentalHealthMedicalReviewCreateA(request)
    }

    override suspend fun ncdPatientDiagnosisStatus(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.ncdPatientDiagnosisStatus(request)
    }

    override suspend fun ncdMentalHealthMedicalReviewCreateS(request: JsonObject): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.ncdMentalHealthMedicalReviewCreateS(request)
    }

    override suspend fun ncdMentalHealthMedicalReviewDetailsA(request: NCDMentalHealthMedicalReviewDetails): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.ncdMentalHealthMedicalReviewDetailsA(request)
    }

    override suspend fun ncdMentalHealthMedicalReviewDetailsS(request: NCDMentalHealthMedicalReviewDetails): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.ncdMentalHealthMedicalReviewDetailsS(request)
    }

    override suspend fun markAsReviewed(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.markAsReviewed(request)
    }

    override suspend fun cultureLocaleUpdate(request: CultureLocaleModel): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.cultureLocaleUpdate(request)
    }

    override suspend fun getVaccinationList(request: RequestVaccinationList): Response<APIResponse<ArrayList<VaccinationDetail>>> {
        return apiService.getImmunisationList(request)
    }

    override suspend fun saveImmunisationList(request: RequestCreateImmunisation): Response<APIResponse<ResponseCreateImmunisation>> {
        return apiService.saveImmunisationList(request)
    }

    override suspend fun getImmunisationSummaryDetails(request: RequestImmunisationSummaryDetail): Response<APIResponse<ResponseImmunisationSummaryDetails>> {
        return apiService.getImmunisationSummaryDetails(request)
    }

    override suspend fun saveImmunisationSummaryDetails(request: RequestImmunisationSummaryCreate): Response<APIResponse<ResponseImmunisationSummaryCreate>> {
        return apiService.saveImmunisationSummaryDetails(request)
    }

    override suspend fun createCommunityProfile(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
       return apiService.createCommunityProfile(request)
    }

    override suspend fun getCommunityProfile(request: HashMap<String, Any>): Response<APIResponse<CommunityProfileDetails>> {
       return apiService.getCommunityProfileDetails(request)
    }

    override suspend fun updateCommunityProfile(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.updateCommunityProfile(request)
    }

    override suspend fun createSupportRequest(request: NCDSupportRequest): Response<APIResponse<String>> {
        return apiService.createSupportRequest(request)
    }

    override suspend fun getTbStaticData(): Response<APIResponse<TbMetaResponse>> {
        return apiService.getTbStaticData()
    }

    override suspend fun createHeight(request: BpAndWeightRequestModel): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createHeight(request)
    }

    override suspend fun fetchHeight(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<BpAndWeightResponse>> {
        return apiService.fetchHeight(motherNeonateAncRequest)
    }

    override suspend fun fetchBmi(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<BpAndWeightResponse>> {
        return apiService.fetchBmi(motherNeonateAncRequest)
    }

    override suspend fun fetchList(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<List<BpAndWeightResponse>>> {
        return apiService.fetchList(motherNeonateAncRequest)
    }

    override suspend fun fetchTbAssessmentDetails(request: MotherNeonateAncRequest): Response<APIResponse<TbHistory>> {
        return apiService.fetchTbAssessmentDetails(request)
    }

    override suspend fun saveTbMedicalReview(request: TbMedicalReviewCreateRequest): Response<APIResponse<PatientEncounterResponse>> {
        return apiService.saveTbMedicalReview(request)
    }

    override suspend fun createPatientType(request: PatientTypeCreateRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createPatientType(request)
    }

    override suspend fun getPatientType(request: MotherNeonateAncRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.getPatientType(request)
    }

    override suspend fun getBirthDetails(request: RequestBirthDetails): Response<APIResponse<BirthDetails>> {
        return apiService.getBirthDetails(request)
    }

    override suspend fun getHivStaticData(): Response<APIResponse<HivMetaResponse>> {
        return apiService.getHIVStaticData()
    }

    override suspend fun createHivScreening(requset: HivScreeningRequest): Response<APIResponse<HivScreeningResponse>> {
        return apiService.createHivScreening(requset)
    }

    override suspend fun getHivScreeningDetails(request: HivScreeningResponse): Response<APIResponse<HivCreateScreeningSummaryResponse>> {
        return apiService.getHivScreeningDetails(request)
    }

    override suspend fun createHivSummary(request: HivMedicalReviewSummaryRequest): Response<APIResponse<HivMedicalReviewSummaryResponse>> {
      return apiService.createHivSummary(request)
    }

    override suspend fun getCBSNotificationDetails(request: PeerSupervisorNotificationRequest): Response<APIResponse<ArrayList<PeerSupervisorNotificationResponse>>> {
        return apiService.getCBSNotificationDetails(request)
    }

    override suspend fun updateCBSNotification(request: PeerSupervisorNotificationRequest): Response<APIResponse<Unit>> {
        return apiService.updateCBSNotification(request)
    }

    override suspend fun createBMI(bpAndWeightRequestModel: BpAndWeightRequestModel): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createBMI(bpAndWeightRequestModel)
    }

    override suspend fun createFamilyPlanningMR(request: FamilyPlanningContraceptivesRequest): Response<APIResponse<FamilyPlanningCreateResponse>> {
        return apiService.createFamilyPlanningMR(request)
    }

    override suspend fun getFamilyPlanningStaticData(): Response<APIResponse<FamilyPlanningMetaResponse>> {
        return apiService.getFamilyPlanningStaticData()
    }

    override suspend fun getFamilyPlanningMRSummaryDetails(request: AboveFiveYearsSummaryRequest): Response<APIResponse<FamilyPlanningSummaryResponse>> {
        return apiService.getFamilyPlanningMRSummaryDetails(request)
    }
    override suspend fun createHivImrCmr(request: HivRequestData): Response<APIResponse<PatientEncounterResponse>> {
        return apiService.createHivImrCmr(request)
    }

    override suspend fun fetchHivSummaryDetails(request: MotherNeonateAncRequest): Response<APIResponse<HivSummaryResponse>> {
        return apiService.fetchHivSummaryDetails(request)
    }

    override suspend fun getViralLoadData(request: ViralLoadRequest): Response<APIResponse<List<ViralLoadResponse>>> {
        return apiService.getViralLoadData(request)
    }
    override suspend fun getARTData(request: ArtRequest): Response<APIResponse<List<ARTResponse>>> {
        return apiService.getARTData(request)
    }

    override suspend fun getOpportunisticInfection(request: MotherNeonateAncRequest): Response<APIResponse<HashMap<String, HashMap<String, String>?>>> {
        return apiService.getOpportunisticInfection(request)
    }

    override suspend fun createEMTCT(request: EMTCTVisitStatusRequest): Response<APIResponse<EMTCTVisitStatusResponse>> {
        return apiService.createEmtct(request)
    }

    override suspend fun getHivVitalsDetails(request: HivVitalsRequest): Response<APIResponse<HivVitalsResponse>> {
        return apiService.getHivVitalsDetails(request)
    }


    override suspend fun createWhoClinicalStage(request: WhoClinicalStageCreateRequest): Response<APIResponse<HivClinicalInfoResponse>> {
        return apiService.createWhoClinicalStage(request)
    }

    override suspend fun getHivVitalDetails(request: HivVitalDetailsRequest): Response<APIResponse<HivVitalDetailsResponse>> {
        return apiService.getHivVitalDetails(request)
    }

    override suspend fun getHivCD4Details(request: CD4DetailsRequest): Response<APIResponse<ArrayList<CD4DetailsResponse>>> {
        return apiService.getHivCD4Details(request)
    }
    override suspend fun getPatientSummaryDetails(request: PregnancySummaryRequest): Response<APIResponse<PregnancyDetailsModel>> {
        return apiService.getPatientSummaryDetails(request)
    }

    override suspend fun checkRecommendationInvestigations(request: MotherNeonateAncRequest): Response<APIResponse<HashMap<String, Boolean?>?>> {
        return apiService.checkRecommendationInvestigations(request)
    }
}