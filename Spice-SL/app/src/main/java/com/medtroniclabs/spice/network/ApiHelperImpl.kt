package com.medtroniclabs.spice.network

import android.content.Context
import com.google.gson.JsonObject
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
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context,
) : ApiHelper {
    override suspend fun doLogin(loginRequest: MultipartBody): Response<LoginResponse> = apiService.doLogin(loginRequest)

    override suspend fun getMetaDataInformation(): Response<APIResponse<MetaDataResponse>> {
        // Mock data - return JSON from assets
//        val jsonString = CommonUtils.getStringFromAssets("mock_static_data.json", context.assets)
//        val type: Type = object : TypeToken<APIResponse<MetaDataResponse>>() {}.type
//        val mockResponse: APIResponse<MetaDataResponse> = Gson().fromJson(jsonString, type)
//        return Response.success(mockResponse)

        // Uncomment below line when backend is ready to use actual API
        return apiService.getMetaDataInformation()
    }

    override suspend fun getForms(formRequest: FormRequest): Response<APIResponse<FormResponse>> = apiService.getForms(formRequest)

    override suspend fun getFormMetadata(request: FormMetaRequest): Response<APIResponse<UserSymptomsEntity>> = apiService.getFormMetadata(request)

    override suspend fun getPatients(request: PatientsDataModel): APIResponse<SearchAndListResponse> = apiService.getPatients(request)

    override suspend fun postOfflineSync(request: Map<String, Any>): Response<SyncResponse> = apiService.postOfflineSyncData(request)

    override suspend fun getOfflineSyncStatus(request: RequestGetSyncStatus): Response<SyncResponse> = apiService.getOfflineSyncStatus(request)

    override suspend fun fetchSyncedData(request: RequestAllEntities): Response<ResponseBody> = apiService.fetchSyncedData(request)

    override suspend fun getHouseholdAndMembers(request: RequestAllEntities): Response<APIResponse<List<HouseHold>>> = apiService.getHouseholdDetails(request)

    override suspend fun patientSearch(request: PatientsDataModel): APIResponse<SearchAndListResponse> = apiService.patientSearch(request)

    override suspend fun getPatient(request: PatientDetailRequest): Response<APIResponse<PatientListRespModel>> = apiService.getPatient(request)

    override suspend fun getAboveFiveYearsMetaData(): Response<APIResponse<AboveFiveYearsMetaResponse>> = apiService.getAboveFiveYearsMetaData()

    override suspend fun getReferralsDetails(request: ReferralDetailRequest): Response<APIResponse<ReferralData>> = apiService.getReferralsDetails(request)

    override suspend fun createAboveFiveYearsResult(request: AboveFiveYearsSubmitRequest): Response<APIResponse<AboveFiveYearsSummaryDetails>> =
        apiService.createAboveFiveYearsResult(request)

    override suspend fun getAboveFiveYearsSummaryDetails(patientId: AboveFiveYearsSummaryRequest): Response<APIResponse<AboveFiveYearsSummaryDetails>> =
        apiService.getAboveFiveYearsSummaryDetails(patientId)

    override suspend fun getMotherNeoNateAncStaticData(): Response<APIResponse<MotherNeonateAncMetaResponse>> = apiService.getMotherNeoNateAncStaticData()

    override suspend fun getMotherPncStaticData(): Response<APIResponse<MotherPncResponse>> = apiService.getMotherPncStaticData()

    override suspend fun getNeonatePncStaticData(): Response<APIResponse<NeonatePncResponse>> = apiService.getNeonatePncStaticData()

    override suspend fun getUnderTwoMonthsMetaData(): Response<APIResponse<UnderTwoMonthsMetaResponse>> = apiService.getUnderTwoMonthsMetaData()

    override suspend fun createSummarySubmit(request: MedicalReviewSummarySubmitRequest): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createSummarySubmit(request)

    override suspend fun getLabourDeliveryMetaData(): Response<APIResponse<LabourDeliveryMetaResponse>> = apiService.getLabourDeliveryMetaData()

    override suspend fun getLabourDeliverySummaryDetails(request: LabourDeliverySummaryDetails): Response<APIResponse<CreateLabourDeliveryRequest>> =
        apiService.getLabourDeliverySummaryDetails(request)

    override suspend fun getPatientStatus(request: PatientStatusRequest): Response<APIResponse<PatientStatusResponse>> = apiService.getPatientStatus(request)

    override suspend fun searchMedicationByName(request: MedicationSearchRequest): Response<APIResponse<ArrayList<MedicationResponse>>> =
        apiService.searchMedicationByName(request)

    override suspend fun searchMedicationGroupByName(request: MedicationGroupSearchRequest): Response<APIResponse<ArrayList<MedicationResponse>>> =
        apiService.searchMedicationGroupByName(request)

    override suspend fun createMedicalReviewForUnderTwoMonths(request: CreateUnderTwoMonthsRequest): Response<APIResponse<CreateUnderTwoMonthsResponse>> =
        apiService.createMedicalReviewForUnderTwoMonths(request)

    override suspend fun getMedicalReviewForUnderTwoMonths(request: CreateUnderTwoMonthsResponse): Response<APIResponse<SummaryDetails>> =
        apiService.getUnderTwoMonthsSummaryDetails(request)

    override suspend fun saveMotherNeonateAnc(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<PatientEncounterResponse>> =
        apiService.saveMotherNeonateAnc(motherNeonateAncRequest)

    override suspend fun saveMotherNeonatePnc(motherNeonatePncRequest: MotherNeonatePncRequest): Response<APIResponse<PncSubmitResponse>> =
        apiService.saveMotherNeonatePnc(motherNeonatePncRequest)

    override suspend fun fetchSummary(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<MotherNeonateAncSummaryModel>> =
        apiService.fetchSummary(motherNeonateAncRequest)

    override suspend fun fetchWeight(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<BpAndWeightResponse>> =
        apiService.fetchWeight(motherNeonateAncRequest)

    override suspend fun fetchBloodPressure(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<BpAndWeightResponse>> =
        apiService.fetchBloodPressure(motherNeonateAncRequest)

    override suspend fun createWeight(bpAndWeightRequestModel: BpAndWeightRequestModel): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createWeight(bpAndWeightRequestModel)

    override suspend fun createBloodPressure(bpAndWeightRequestModel: BpAndWeightRequestModel): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createBloodPressure(bpAndWeightRequestModel)

    override suspend fun saveUpdateDiagnosis(request: DiagnosisSaveUpdateRequest): Response<APIResponse<ArrayList<DiagnosisDiseaseModel>>> =
        apiService.saveUpdateDiagnosis(request)

    override suspend fun getDiagnosisDetails(request: CreateUnderTwoMonthsResponse): Response<APIResponse<ArrayList<DiagnosisDiseaseModel>>> =
        apiService.getDiagnosisDetails(request)

    override suspend fun getHealthFacilityMetaData(request: ReferPatientAPIRequest): Response<APIResponse<List<ReferPatientHealthFacilityItem>>> =
        apiService.getHealthFacilityMetaData(request)

    override suspend fun getReferPatientMobileUserList(tenantId: ReferPatientRequest): Response<APIResponse<List<ReferPatientNameNumber>>> =
        apiService.getReferPatientMobileUserList(tenantId)

    override suspend fun createReferPatientResult(request: ReferPatientResult): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createReferPatientResult(request)

    override suspend fun createPrescriptionRequest(request: RequestBody): Response<APIResponse<Map<String, Any>>> =
        apiService.createPrescriptionRequest(request)

    override suspend fun getPrescriptionList(request: PrescriptionListRequest): Response<APIResponse<ArrayList<Prescription>>> =
        apiService.getPrescriptionList(request)

    override suspend fun removePrescription(request: RemovePrescriptionRequest): Response<APIResponse<Map<String, Any>>> =
        apiService.removePrescription(request)

    override suspend fun removeCommunityPrescription(request: List<RemovePrescriptionRequest>): Response<APIResponse<Map<String, Any>>> =
        apiService.removeCommunityPrescription(request)

    override suspend fun getUnderFiveYearsMetaData(): Response<APIResponse<UnderFiveYearsMetaResponse>> = apiService.getUnderFiveYearsMetaData()

    override suspend fun createMedicalReviewForUnderFiveYears(request: CreateUnderFiveYearsRequest): Response<APIResponse<CreateUnderTwoMonthsResponse>> =
        apiService.createMedicalReviewForUnderFiveYears(request)

    override suspend fun getUnderFiveYearsSummaryDetails(request: CreateUnderTwoMonthsResponse): Response<APIResponse<SummaryDetails>> =
        apiService.getUnderFiveYearsSummaryDetails(request)

    override suspend fun getPrescription(request: ReferralDetailRequest): Response<APIResponse<HistoryEntity>> = apiService.getPrescription(request)

    override suspend fun getMedicalReviewHistory(request: ReferralDetailRequest): Response<APIResponse<MedicalReviewHistory>> =
        apiService.getMedicalReviewHistory(request)

    override suspend fun getMedicalReviewHistoryPNC(request: ReferralDetailRequest): Response<APIResponse<PncChildMedicalReview>> =
        apiService.getMedicalReviewHistoryPNC(request)

    override suspend fun getPncSummaryDetails(request: MotherNeonatePncSummaryRequest): Response<APIResponse<MotherNeonatePncSummaryResponse>> =
        apiService.getPncSummaryDetails(request)

    override suspend fun summaryCreatePncData(summaryCreateRequest: SummaryCreateRequest): Response<APIResponse<HashMap<String, Any>>> =
        apiService.summaryCreatePncData(summaryCreateRequest)

    override suspend fun getBirthHistoryDetails(request: BirthHistoryRequest): Response<APIResponse<BirthHistoryResponse>> =
        apiService.getBirthHistoryDetails(request)

    override suspend fun createMedicalReviewLabourDelivery(request: CreateLabourDeliveryRequest): Response<APIResponse<CreateLabourDeliveryResponse>> =
        apiService.createMedicalReviewForLaborDelivery(request)

    override suspend fun addNewMember(request: AddMemberRegRequest): Response<APIResponse<String>> = apiService.addNewMember(request)

    override suspend fun searchLabTestByName(request: SearchRequestLabTest): Response<APIResponse<ArrayList<SearchLabTestResponse>>> =
        apiService.searchLabTestByName(request)

    override suspend fun createLabTest(request: LabTestCreateRequest): Response<APIResponse<Map<String, Any>>> = apiService.createLabTest(request)

    override suspend fun updateLabTest(request: LabTestCreateRequest): Response<APIResponse<Map<String, Any>>> = apiService.updateLabTest(request)

    override suspend fun getLabTestList(request: LabTestListRequest): Response<APIResponse<ArrayList<LabTestListResponse>>> = apiService.getLabTestList(request)

    override suspend fun removeLabTest(request: RemoveLabTestRequest): Response<APIResponse<Map<String, Any>>> = apiService.removeLabTest(request)

    override suspend fun createSummaryMotherNeonate(request: LabourDeliverySummaryRequest): Response<APIResponse<HashMap<String, Any>>> =
        apiService.summaryCreateMotherNeonate(request)

    override suspend fun getPeerSupervisorLinkedChwList(): Response<APIResponse<List<ChwVillageFilterModel>>> = apiService.getPeerSupervisorLinkedChwList()

    override suspend fun getPeerSupervisorReport(request: PerformanceReportRequest): Response<APIResponse<List<CHWPerformanceMonitoring>>> =
        apiService.getPeerSupervisorReport(request)

    override suspend fun getUserFilterPreference(request: FilterPreference): Response<APIResponse<FilterPreference>> =
        apiService.getUserFilterPreference(request)

    override suspend fun saveUserFilterPreference(request: FilterPreference): Response<APIResponse<FilterPreference>> =
        apiService.saveUserFilterPreference(request)

    override suspend fun getInvestigation(request: ReferralDetailRequest): Response<APIResponse<HistoryEntity>> =
        apiService.getInvestigationHistoryList(request)

    override suspend fun forgotPassword(
        email: String,
        clientConstant: String,
    ): Response<APIResponse<String?>> = apiService.forgotPassword(email, clientConstant)

    override suspend fun verifyToken(token: String): Response<APIResponse<String?>> = apiService.verifyToken(token)

    override suspend fun resetPassword(
        token: String,
        request: RequestChangePassword,
    ): Response<APIResponse<ResponseChangePassword>> = apiService.resetPassword(token, request)

    override suspend fun uploadAllConsentSignatures(request: RequestBody): Response<APIResponse<List<ResponseSignatureUpload>>> =
        apiService.uploadAllConsentSignatures(request)

    override suspend fun checkAppVersion(): Response<APIResponse<Boolean>> = apiService.checkAppVersion()

    override suspend fun registerPatient(hashMap: RequestBody): Response<APIResponse<RegistrationResponse>> = apiService.registerPatient(hashMap)

    override suspend fun createScreening(createRequest: RequestBody): Response<APIResponse<HashMap<String, Any>>> = apiService.createScreening(createRequest)

    override suspend fun getNcdMRStaticData(): Response<APIResponse<NcdMRStaticDataModel>> = apiService.getNcdMRStaticData()

    override suspend fun bpLogCreate(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> = apiService.bpLogCreate(request)

    override suspend fun glucoseLogCreate(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> = apiService.glucoseLogCreate(request)

    override suspend fun bpLogList(request: BPBGListModel): Response<APIResponse<BPBGListModel>> = apiService.bpLogList(request)

    override suspend fun glucoseLogList(request: BPBGListModel): Response<APIResponse<BPBGListModel>> = apiService.glucoseLogList(request)

    override suspend fun createAssessmentNCD(request: JsonObject): Response<HashMap<String, Any>> = apiService.createAssessmentNCD(request)

    override suspend fun ncdPregnancyCreate(request: PregnancyDetailsModel): Response<APIResponse<HashMap<String, Any>>> =
        apiService.ncdPregnancyCreate(request)

    override suspend fun ncdPregnancyDetails(request: HashMap<String, Any>): Response<APIResponse<PregnancyDetailsModel>> =
        apiService.ncdPregnancyDetails(request)

    override suspend fun createPatientVisit(request: PatientVisitRequest): Response<APIResponse<PatientVisitResponse>> = apiService.createPatientVisit(request)

    override suspend fun createNCDMedicalReview(request: MedicalReviewRequestResponse): Response<APIResponse<MedicalReviewResponse>> =
        apiService.createNCDMedicalReview(request)

    override suspend fun fetchNCDMRSummary(request: MedicalReviewResponse): Response<APIResponse<MRSummaryResponse>> = apiService.fetchNCDMRSummary(request)

    override suspend fun getPatientPrescriptionHistoryList(request: RemovePrescriptionRequest): Response<APIResponse<ArrayList<Prescription>>> =
        apiService.getPatientPrescriptionHistoryList(request)

    override suspend fun createConfirmDiagonsis(request: NCDDiagnosisRequestResponse): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createConfirmDiagonsis(request)

    override suspend fun getConfirmDiagonsis(request: NCDDiagnosisGetRequest): Response<APIResponse<NCDDiagnosisGetResponse>> =
        apiService.getConfirmDiagonsis(request)

    override suspend fun createNCDPatientStatus(request: NCDPatientStatusRequest): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createNCDPatientStatus(request)

    override suspend fun updateNCDTreatmentPlan(request: NCDTreatmentPlanModel): Response<APIResponse<NCDTreatmentPlanModel>> =
        apiService.updateNCDTreatmentPlan(request)

    override suspend fun getNCDTreatmentPlan(request: NCDTreatmentPlanModelDetails): Response<APIResponse<NCDTreatmentPlanModelDetails>> =
        apiService.getNCDTreatmentPlan(request)

    override suspend fun createNCDMRSummaryCreate(request: NCDMRSummaryRequestResponse): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createNCDMRSummaryCreate(request)

    override suspend fun getPrescriptionDispenseList(request: DispenseUpdateRequest): Response<APIResponse<ArrayList<DispensePrescriptionResponse>>> =
        apiService.getPrescriptionDispenseList(request)

    override suspend fun updateDispensePrescription(request: DispensePrescriptionRequest): Response<APIResponse<DispenseUpdateResponse>> =
        apiService.updateDispensePrescription(request)

    override suspend fun getDispensePrescriptionHistory(request: DispenseUpdateRequest): Response<APIResponse<ArrayList<DispensePrescriptionResponse>>> =
        apiService.getDispensePrescriptionHistory(request)

    override suspend fun createLifestyle(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>> = apiService.createLifestyle(request)

    override suspend fun updateLifestyle(request: AssessmentResultModel): Response<APIResponse<HashMap<String, Any>>> = apiService.updateLifestyle(request)

    override suspend fun getLifestyleList(request: NCDCounselingModel): Response<APIResponse<ArrayList<NCDCounselingModel>>> =
        apiService.getLifestyleList(request)

    override suspend fun removeLifestyle(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>> = apiService.removeLifestyle(request)

    override suspend fun createPsychological(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>> = apiService.createPsychological(request)

    override suspend fun updatePsychological(request: AssessmentResultModel): Response<APIResponse<HashMap<String, Any>>> =
        apiService.updatePsychological(request)

    override suspend fun getPsychological(request: NCDCounselingModel): Response<APIResponse<ArrayList<NCDCounselingModel>>> =
        apiService.getPsychological(request)

    override suspend fun removePsychological(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>> = apiService.removePsychological(request)

    override suspend fun getNCDMedicalReviewHistory(request: ReferralDetailRequest): Response<APIResponse<NCDMedicalReviewHistory>> =
        apiService.getNCDMedicalReviewHistory(request)

    override suspend fun validatePatient(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> = apiService.validatePatient(request)

    override suspend fun ncdGetInstructions(): Response<APIResponse<NCDInstructionModel>> = apiService.ncdGetInstructions()

    override suspend fun ncdUpdatePregnancyRisk(request: NCDPregnancyRiskUpdate): Response<APIResponse<Boolean>> = apiService.ncdUpdatePregnancyRisk(request)

    override suspend fun getWazWhzScore(request: WazWhzScoreRequest): Response<APIResponse<WazWhzScoreResponse>> = apiService.getWazWhzScore(request)

    override suspend fun getUserDashboardDetails(request: NCDUserDashboardRequest): Response<APIResponse<NCDUserDashboardResponse>> =
        apiService.getUserDashboardDetails(request)

    override suspend fun getBadgeNotifications(request: BadgeNotificationModel): Response<APIResponse<BadgeNotificationModel>> =
        apiService.getBadgeNotifications(request)

    override suspend fun updateBadgeNotifications(request: BadgeNotificationModel): Response<APIResponse<Boolean>> =
        apiService.updateBadgeNotifications(request)

    override suspend fun getNcdLifeStyleDetails(request: LifeStyleRequest): Response<APIResponse<ArrayList<LifeStyleResponse>>> =
        apiService.getNcdLifeStyleDetails(request)

    override suspend fun ncdPatientRemove(request: NCDPatientRemoveRequest): Response<APIResponse<Boolean>> = apiService.ncdPatientRemove(request)

    override suspend fun bpLogCreateForNurse(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> =
        apiService.bpLogCreateForNurse(request)

    override suspend fun glucoseLogCreateForNurse(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> =
        apiService.glucoseLogCreateForNurse(request)

    override suspend fun ncdUpdatePatientDetail(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> =
        apiService.ncdUpdatePatientDetail(request)

    override suspend fun getUserTermsAndConditions(request: TermsAndConditionsModel): Response<APIResponse<TermsAndConditionsModel>> =
        apiService.getUserTermsAndConditions(request)

    override suspend fun updateTermsAndConditionsStatus(request: TermsAndConditionsModel): Response<APIResponse<TermsAndConditionsModel>> =
        apiService.updateTermsAndConditionsStatus(request)

    override suspend fun ncdUpdateNextVisitDate(request: NCDMedicalReviewUpdateModel): Response<APIResponse<HashMap<String, Any>>> =
        apiService.ncdUpdateNextVisitDate(request)

    override suspend fun validatePatientTransfer(request: NCDPatientTransferValidate): Response<HashMap<String, Any>> =
        apiService.validatePatientTransfer(request)

    override suspend fun createPatientTransfer(request: NCDTransferCreateRequest): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createPatientTransfer(request)

    override suspend fun searchSite(request: NCDRegionSiteModel): Response<APIResponse<ArrayList<RegionSiteResponse>>> = apiService.searchSite(request)

    override suspend fun searchRoleUser(request: NCDSiteRoleModel): Response<APIResponse<ArrayList<NCDSiteRoleResponse>>> = apiService.searchRoleUser(request)

    override suspend fun getPatientListTransfer(request: NCDPatientTransferNotificationCountRequest): Response<APIResponse<PatientTransferListResponse>> =
        apiService.getPatientListTransfer(request)

    override suspend fun patientTransferNotificationCount(
        request: NCDPatientTransferNotificationCountRequest,
    ): Response<APIResponse<NCDPatientTransferNotificationCountResponse>> = apiService.patientTransferNotificationCount(request)

    override suspend fun patientTransferUpdate(request: NCDPatientTransferUpdateRequest): Response<APIResponse<String>> =
        apiService.patientTransferUpdate(request)

    override suspend fun getNudgesList(prescriptionNudgeRequest: PredictionRequest): Response<APIResponse<PrescriptionNudgeResponse>> =
        apiService.getNudgesList(prescriptionNudgeRequest)

    override suspend fun getLabTestNudgeList(predictionRequest: PredictionRequest): Response<APIResponse<HashMap<String, Any>>> =
        apiService.getLabTestNudgeList(predictionRequest)

    override suspend fun ncdFollowUpList(request: FollowUpRequest): APIResponse<List<PatientFollowUpEntity>> = apiService.ncdFollowUpList(request)

    override suspend fun getPatientCallRegister(): Response<APIResponse<RegisterCallResponse>> = apiService.getPatientCallRegister()

    override suspend fun updatePatientCallRegister(request: FollowUpUpdateRequest): Response<APIResponse<HashMap<String, Any>>> =
        apiService.updatePatientCallRegister(request)

    override suspend fun updateDeviceDetails(request: DeviceDetails): Response<APIResponse<DeviceDetails>> = apiService.updateDeviceDetails(request)

    override suspend fun createMentalHealthStatus(request: NCDMentalHealthStatusRequest): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createMentalHealthStatus(request)

    override suspend fun ncdMentalHealthMedicalReviewCreateA(request: JsonObject): Response<APIResponse<HashMap<String, Any>>> =
        apiService.ncdMentalHealthMedicalReviewCreateA(request)

    override suspend fun ncdPatientDiagnosisStatus(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> =
        apiService.ncdPatientDiagnosisStatus(request)

    override suspend fun ncdMentalHealthMedicalReviewCreateS(request: JsonObject): Response<APIResponse<HashMap<String, Any>>> =
        apiService.ncdMentalHealthMedicalReviewCreateS(request)

    override suspend fun ncdMentalHealthMedicalReviewDetailsA(request: NCDMentalHealthMedicalReviewDetails): Response<APIResponse<HashMap<String, Any>>> =
        apiService.ncdMentalHealthMedicalReviewDetailsA(request)

    override suspend fun ncdMentalHealthMedicalReviewDetailsS(request: NCDMentalHealthMedicalReviewDetails): Response<APIResponse<HashMap<String, Any>>> =
        apiService.ncdMentalHealthMedicalReviewDetailsS(request)

    override suspend fun markAsReviewed(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> = apiService.markAsReviewed(request)

    override suspend fun cultureLocaleUpdate(request: CultureLocaleModel): Response<APIResponse<HashMap<String, Any>>> = apiService.cultureLocaleUpdate(request)

    override suspend fun getVaccinationList(request: RequestVaccinationList): Response<APIResponse<ArrayList<VaccinationDetail>>> =
        apiService.getImmunisationList(request)

    override suspend fun saveImmunisationList(request: RequestCreateImmunisation): Response<APIResponse<ResponseCreateImmunisation>> =
        apiService.saveImmunisationList(request)

    override suspend fun getImmunisationSummaryDetails(request: RequestImmunisationSummaryDetail): Response<APIResponse<ResponseImmunisationSummaryDetails>> =
        apiService.getImmunisationSummaryDetails(request)

    override suspend fun saveImmunisationSummaryDetails(request: RequestImmunisationSummaryCreate): Response<APIResponse<ResponseImmunisationSummaryCreate>> =
        apiService.saveImmunisationSummaryDetails(request)

    override suspend fun createCommunityProfile(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createCommunityProfile(request)

    override suspend fun getCommunityProfile(request: HashMap<String, Any>): Response<APIResponse<CommunityProfileDetails>> =
        apiService.getCommunityProfileDetails(request)

    override suspend fun updateCommunityProfile(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> =
        apiService.updateCommunityProfile(request)

    override suspend fun createSupportRequest(request: NCDSupportRequest): Response<APIResponse<String>> = apiService.createSupportRequest(request)

    override suspend fun getTbStaticData(): Response<APIResponse<TbMetaResponse>> = apiService.getTbStaticData()

    override suspend fun createHeight(request: BpAndWeightRequestModel): Response<APIResponse<HashMap<String, Any>>> = apiService.createHeight(request)

    override suspend fun fetchHeight(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<BpAndWeightResponse>> =
        apiService.fetchHeight(motherNeonateAncRequest)

    override suspend fun fetchBmi(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<BpAndWeightResponse>> =
        apiService.fetchBmi(motherNeonateAncRequest)

    override suspend fun fetchList(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<List<BpAndWeightResponse>>> =
        apiService.fetchList(motherNeonateAncRequest)

    override suspend fun fetchTbAssessmentDetails(request: MotherNeonateAncRequest): Response<APIResponse<TbHistory>> =
        apiService.fetchTbAssessmentDetails(request)

    override suspend fun saveTbMedicalReview(request: TbMedicalReviewCreateRequest): Response<APIResponse<PatientEncounterResponse>> =
        apiService.saveTbMedicalReview(request)

    override suspend fun createPatientType(request: PatientTypeCreateRequest): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createPatientType(request)

    override suspend fun getPatientType(request: MotherNeonateAncRequest): Response<APIResponse<HashMap<String, Any>>> = apiService.getPatientType(request)

    override suspend fun getBirthDetails(request: RequestBirthDetails): Response<APIResponse<BirthDetails>> = apiService.getBirthDetails(request)

    override suspend fun getHivStaticData(): Response<APIResponse<HivMetaResponse>> = apiService.getHIVStaticData()

    override suspend fun createHivScreening(requset: HivScreeningRequest): Response<APIResponse<HivScreeningResponse>> = apiService.createHivScreening(requset)

    override suspend fun getHivScreeningDetails(request: HivScreeningResponse): Response<APIResponse<HivCreateScreeningSummaryResponse>> =
        apiService.getHivScreeningDetails(request)

    override suspend fun getCBSNotificationDetails(
        request: PeerSupervisorNotificationRequest,
    ): Response<APIResponse<ArrayList<PeerSupervisorNotificationResponse>>> = apiService.getCBSNotificationDetails(request)

    override suspend fun updateCBSNotification(request: PeerSupervisorNotificationRequest): Response<APIResponse<Unit>> =
        apiService.updateCBSNotification(request)

    override suspend fun createBMI(bpAndWeightRequestModel: BpAndWeightRequestModel): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createBMI(bpAndWeightRequestModel)

    override suspend fun createFamilyPlanningMR(request: FamilyPlanningContraceptivesRequest): Response<APIResponse<FamilyPlanningCreateResponse>> =
        apiService.createFamilyPlanningMR(request)

    override suspend fun getFamilyPlanningStaticData(): Response<APIResponse<FamilyPlanningMetaResponse>> = apiService.getFamilyPlanningStaticData()

    override suspend fun getFamilyPlanningMRSummaryDetails(request: AboveFiveYearsSummaryRequest): Response<APIResponse<FamilyPlanningSummaryResponse>> =
        apiService.getFamilyPlanningMRSummaryDetails(request)

    override suspend fun createHivImrCmr(request: HivRequestData): Response<APIResponse<PatientEncounterResponse>> = apiService.createHivImrCmr(request)

    override suspend fun fetchHivSummaryDetails(request: MotherNeonateAncRequest): Response<APIResponse<HivSummaryResponse>> =
        apiService.fetchHivSummaryDetails(request)

    override suspend fun getViralLoadData(request: ViralLoadRequest): Response<APIResponse<List<ViralLoadResponse>>> = apiService.getViralLoadData(request)

    override suspend fun getARTData(request: ArtRequest): Response<APIResponse<List<ARTResponse>>> = apiService.getARTData(request)

    override suspend fun getOpportunisticInfection(request: MotherNeonateAncRequest): Response<APIResponse<HashMap<String, HashMap<String, String>?>>> =
        apiService.getOpportunisticInfection(request)

    override suspend fun createEMTCT(request: EMTCTVisitStatusRequest): Response<APIResponse<EMTCTVisitStatusResponse>> = apiService.createEmtct(request)

    override suspend fun getHivVitalsDetails(request: HivVitalsRequest): Response<APIResponse<HivVitalsResponse>> = apiService.getHivVitalsDetails(request)

    override suspend fun createWhoClinicalStage(request: WhoClinicalStageCreateRequest): Response<APIResponse<HivClinicalInfoResponse>> =
        apiService.createWhoClinicalStage(request)

    override suspend fun getHivCD4Details(request: CD4DetailsRequest): Response<APIResponse<ArrayList<CD4DetailsResponse>>> =
        apiService.getHivCD4Details(request)

    override suspend fun getPatientSummaryDetails(request: PregnancySummaryRequest): Response<APIResponse<PregnancyDetailsModel>> =
        apiService.getPatientSummaryDetails(request)

    override suspend fun checkRecommendationInvestigations(request: MotherNeonateAncRequest): Response<APIResponse<HashMap<String, Boolean?>?>> =
        apiService.checkRecommendationInvestigations(request)
}
