package com.medtroniclabs.spice.network

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
import com.medtroniclabs.spice.data.FormMetaRequest
import com.medtroniclabs.spice.data.FormRequest
import com.medtroniclabs.spice.data.FormResponse
import com.medtroniclabs.spice.data.LabourDeliveryMetaResponse
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.MedicalReviewSummarySubmitRequest
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
import com.medtroniclabs.spice.data.UnderFiveYearsMetaResponse
import com.medtroniclabs.spice.data.UnderTwoMonthsMetaResponse
import com.medtroniclabs.spice.data.UserSymptomsEntity
import com.medtroniclabs.spice.data.history.HistoryEntity
import com.medtroniclabs.spice.data.history.MedicalReviewHistory
import com.medtroniclabs.spice.data.history.NCDMedicalReviewHistory
import com.medtroniclabs.spice.data.model.AboveFiveYearsSubmitRequest
import com.medtroniclabs.spice.data.model.BpAndWeightRequestModel
import com.medtroniclabs.spice.data.model.BpAndWeightResponse
import com.medtroniclabs.spice.data.model.CreateLabourDeliveryRequest
import com.medtroniclabs.spice.data.model.CreateLabourDeliveryResponse
import com.medtroniclabs.spice.data.model.LabourDeliverySummaryDetails
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.MotherNeonatePncRequest
import com.medtroniclabs.spice.data.model.PatientEncounterResponse
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferNotificationCountRequest
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferNotificationCountResponse
import com.medtroniclabs.spice.data.model.PncSubmitResponse
import com.medtroniclabs.spice.data.model.RegistrationResponse
import com.medtroniclabs.spice.data.model.RequestChangePassword
import com.medtroniclabs.spice.data.model.ResponseChangePassword
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.model.ResponseSignatureUpload
import com.medtroniclabs.spice.data.offlinesync.model.SyncResponse
import com.medtroniclabs.spice.data.performance.CHWPerformanceMonitoring
import com.medtroniclabs.spice.data.performance.ChwVillageFilterModel
import com.medtroniclabs.spice.data.performance.FilterPreference
import com.medtroniclabs.spice.data.performance.PerformanceReportRequest
import com.medtroniclabs.spice.data.resource.LabourDeliverySummaryRequest
import com.medtroniclabs.spice.data.resource.RequestAllEntities
import com.medtroniclabs.spice.model.LabTestCreateRequest
import com.medtroniclabs.spice.model.LabTestListRequest
import com.medtroniclabs.spice.model.LabTestListResponse
import com.medtroniclabs.spice.model.NcdMRStaticDataModel
import com.medtroniclabs.spice.model.PatientDetailRequest
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.PatientsDataModel
import com.medtroniclabs.spice.model.ReferralData
import com.medtroniclabs.spice.model.ReferralDetailRequest
import com.medtroniclabs.spice.model.RemoveLabTestRequest
import com.medtroniclabs.spice.model.SearchAndListResponse
import com.medtroniclabs.spice.model.medicalreview.AddMemberRegRequest
import com.medtroniclabs.spice.model.medicalreview.CreateUnderFiveYearsRequest
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsRequest
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.SearchLabTestResponse
import com.medtroniclabs.spice.model.medicalreview.SearchRequestLabTest
import com.medtroniclabs.spice.model.medicalreview.SummaryDetails
import com.medtroniclabs.spice.ncd.counseling.model.AssessmentResultModel
import com.medtroniclabs.spice.ncd.counseling.model.NCDCounselingModel
import com.medtroniclabs.spice.ncd.data.BPBGListModel
import com.medtroniclabs.spice.ncd.data.BadgeNotificationModel
import com.medtroniclabs.spice.ncd.data.LabTestPredictionResponse
import com.medtroniclabs.spice.ncd.data.MRSummaryResponse
import com.medtroniclabs.spice.ncd.data.MedicalReviewRequestResponse
import com.medtroniclabs.spice.ncd.data.MedicalReviewResponse
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisGetRequest
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisGetResponse
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisRequestResponse
import com.medtroniclabs.spice.ncd.data.NCDInstructionModel
import com.medtroniclabs.spice.ncd.data.NCDMRSummaryRequestResponse
import com.medtroniclabs.spice.ncd.data.NCDPatientStatusRequest
import com.medtroniclabs.spice.ncd.data.NCDPregnancyRiskUpdate
import com.medtroniclabs.spice.ncd.data.NCDTreatmentPlanModel
import com.medtroniclabs.spice.ncd.data.NCDTreatmentPlanModelDetails
import com.medtroniclabs.spice.ncd.data.LifeStyleResponse
import com.medtroniclabs.spice.ncd.data.LifeStyleRequest
import com.medtroniclabs.spice.ncd.data.NCDMedicalReviewUpdateModel
import com.medtroniclabs.spice.ncd.data.PatientVisitRequest
import com.medtroniclabs.spice.ncd.data.PatientVisitResponse
import com.medtroniclabs.spice.ncd.data.NCDPatientRemoveRequest
import com.medtroniclabs.spice.ncd.data.TermsAndConditionsModel
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferValidate
import com.medtroniclabs.spice.ncd.data.NCDTransferCreateRequest
import com.medtroniclabs.spice.ncd.data.PatientTransferListResponse
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferUpdateRequest
import com.medtroniclabs.spice.ncd.data.NCDRegionSiteModel
import com.medtroniclabs.spice.ncd.data.RegionSiteResponse
import com.medtroniclabs.spice.ncd.data.NCDSiteRoleModel
import com.medtroniclabs.spice.ncd.data.NCDSiteRoleResponse
import com.medtroniclabs.spice.ncd.data.PredictionRequest
import com.medtroniclabs.spice.ncd.data.PrescriptionNudgeResponse
import com.medtroniclabs.spice.model.medicalreview.WazWhzScoreRequest
import com.medtroniclabs.spice.model.medicalreview.WazWhzScoreResponse
import com.medtroniclabs.spice.ncd.data.FollowUpRequest
import com.medtroniclabs.spice.ncd.data.FollowUpUpdateRequest
import com.medtroniclabs.spice.ncd.data.PatientFollowUpEntity
import com.medtroniclabs.spice.ncd.data.RegisterCallResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response

interface ApiHelper {
    suspend fun doLogin(loginRequest: MultipartBody): Response<LoginResponse>
    suspend fun getMetaDataInformation(): Response<APIResponse<MetaDataResponse>>
    suspend fun getForms(formRequest: FormRequest): Response<APIResponse<FormResponse>>
    suspend fun getFormMetadata(request: FormMetaRequest): Response<APIResponse<UserSymptomsEntity>>
    suspend fun postOfflineSync(request: Map<String, Any>): Response<SyncResponse>
    suspend fun getOfflineSyncStatus(request: RequestGetSyncStatus): Response<SyncResponse>
    suspend fun getHouseholdAndMembers(request: RequestAllEntities): Response<APIResponse<List<HouseHold>>>

    suspend fun fetchSyncedData(request: RequestAllEntities): Response<ResponseBody>

    suspend fun getPatients(request: PatientsDataModel): APIResponse<SearchAndListResponse>
    suspend fun patientSearch(request: PatientsDataModel): APIResponse<SearchAndListResponse>
    suspend fun getPatient(request: PatientDetailRequest): Response<APIResponse<PatientListRespModel>>
    suspend fun getAboveFiveYearsMetaData(): Response<APIResponse<AboveFiveYearsMetaResponse>>
    suspend fun getReferralsDetails(request: ReferralDetailRequest): Response<APIResponse<ReferralData>>
    suspend fun createAboveFiveYearsResult(request: AboveFiveYearsSubmitRequest): Response<APIResponse<AboveFiveYearsSummaryDetails>>
    suspend fun getAboveFiveYearsSummaryDetails(patientId: AboveFiveYearsSummaryRequest): Response<APIResponse<AboveFiveYearsSummaryDetails>>
    suspend fun getMotherNeoNateAncStaticData(): Response<APIResponse<MotherNeonateAncMetaResponse>>
    suspend fun getMotherPncStaticData(): Response<APIResponse<MotherPncResponse>>
    suspend fun getNeonatePncStaticData(): Response<APIResponse<NeonatePncResponse>>

    suspend fun getUnderTwoMonthsMetaData(): Response<APIResponse<UnderTwoMonthsMetaResponse>>
    suspend fun createSummarySubmit(request: MedicalReviewSummarySubmitRequest): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getLabourDeliveryMetaData(): Response<APIResponse<LabourDeliveryMetaResponse>>
    suspend fun getLabourDeliverySummaryDetails(request: LabourDeliverySummaryDetails): Response<APIResponse<CreateLabourDeliveryRequest>>
    suspend fun getPatientStatus(request: PatientStatusRequest): Response<APIResponse<PatientStatusResponse>>
    suspend fun searchMedicationByName(request: MedicationSearchRequest): Response<APIResponse<ArrayList<MedicationResponse>>>
    suspend fun createMedicalReviewForUnderTwoMonths(request: CreateUnderTwoMonthsRequest): Response<APIResponse<CreateUnderTwoMonthsResponse>>
    suspend fun getMedicalReviewForUnderTwoMonths(request: CreateUnderTwoMonthsResponse): Response<APIResponse<SummaryDetails>>
    suspend fun saveMotherNeonateAnc(motherNeonateAncRequest: MotherNeonateAncRequest):Response<APIResponse<PatientEncounterResponse>>
    suspend fun saveMotherNeonatePnc(motherNeonatePncRequest: MotherNeonatePncRequest):Response<APIResponse<PncSubmitResponse>>
    suspend fun fetchSummary(motherNeonateAncRequest : MotherNeonateAncRequest) : Response<APIResponse<MotherNeonateAncSummaryModel>>
    suspend fun fetchWeight(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<BpAndWeightResponse>>
    suspend fun fetchBloodPressure(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<BpAndWeightResponse>>
    suspend fun createWeight(bpAndWeightRequestModel: BpAndWeightRequestModel): Response<APIResponse<HashMap<String, Any>>>
    suspend fun createBloodPressure(bpAndWeightRequestModel: BpAndWeightRequestModel): Response<APIResponse<HashMap<String, Any>>>
    suspend fun saveUpdateDiagnosis(request: DiagnosisSaveUpdateRequest): Response<APIResponse<ArrayList<DiagnosisDiseaseModel>>>
    suspend fun getDiagnosisDetails(request: CreateUnderTwoMonthsResponse): Response<APIResponse<ArrayList<DiagnosisDiseaseModel>>>
    suspend fun getHealthFacilityMetaData(request: ReferPatientAPIRequest): Response<APIResponse<List<ReferPatientHealthFacilityItem>>>
    suspend fun getReferPatientMobileUserList(tenantId: ReferPatientRequest): Response<APIResponse<List<ReferPatientNameNumber>>>
    suspend fun createReferPatientResult(request: ReferPatientResult): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getUnderFiveYearsMetaData(): Response<APIResponse<UnderFiveYearsMetaResponse>>
    suspend fun createMedicalReviewForUnderFiveYears(request: CreateUnderFiveYearsRequest): Response<APIResponse<CreateUnderTwoMonthsResponse>>
    suspend fun getUnderFiveYearsSummaryDetails(request: CreateUnderTwoMonthsResponse): Response<APIResponse<SummaryDetails>>
    suspend fun createPrescriptionRequest(request: RequestBody): Response<APIResponse<Map<String, Any>>>
    suspend fun getPrescriptionList(request: PrescriptionListRequest): Response<APIResponse<ArrayList<Prescription>>>
    suspend fun removePrescription(request: RemovePrescriptionRequest): Response<APIResponse<Map<String, Any>>>
    suspend fun getPrescription(request: ReferralDetailRequest): Response<APIResponse<HistoryEntity>>
    suspend fun getMedicalReviewHistory(request: ReferralDetailRequest): Response<APIResponse<MedicalReviewHistory>>
    suspend fun getPncSummaryDetails(request: MotherNeonatePncSummaryRequest): Response<APIResponse<MotherNeonatePncSummaryResponse>>
    suspend fun summaryCreatePncData(summaryCreateRequest: SummaryCreateRequest):Response<APIResponse<HashMap<String, Any>>>
    suspend fun getBirthHistoryDetails(request: BirthHistoryRequest): Response<APIResponse<BirthHistoryResponse>>
    suspend fun createMedicalReviewLabourDelivery(request: CreateLabourDeliveryRequest): Response<APIResponse<CreateLabourDeliveryResponse>>
    suspend fun searchLabTestByName(request: SearchRequestLabTest): Response<APIResponse<ArrayList<SearchLabTestResponse>>>
    suspend fun createLabTest(request: LabTestCreateRequest): Response<APIResponse<Map<String, Any>>>
    suspend fun getLabTestList(request: LabTestListRequest): Response<APIResponse<ArrayList<LabTestListResponse>>>
    suspend fun removeLabTest(request: RemoveLabTestRequest): Response<APIResponse<Map<String, Any>>>
    suspend fun  addNewMember(request: AddMemberRegRequest) : Response<APIResponse<String>>
    suspend fun createSummaryMotherNeonate(request: LabourDeliverySummaryRequest): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getInvestigation(request: ReferralDetailRequest): Response<APIResponse<HistoryEntity>>
    suspend fun getMedicalReviewHistoryPNC(request: ReferralDetailRequest): Response<APIResponse<PncChildMedicalReview>>
    suspend fun getPeerSupervisorLinkedChwList(): Response<APIResponse<List<ChwVillageFilterModel>>>

    suspend fun getPeerSupervisorReport(request: PerformanceReportRequest): Response<APIResponse<List<CHWPerformanceMonitoring>>>

    suspend fun getUserFilterPreference(request: FilterPreference): Response<APIResponse<FilterPreference>>

    suspend fun saveUserFilterPreference(request: FilterPreference): Response<APIResponse<FilterPreference>>

    suspend fun forgotPassword(email: String, clientConstant: String): Response<APIResponse<String?>>

    suspend fun verifyToken(token: String): Response<APIResponse<String?>>

    suspend fun resetPassword(token: String, request: RequestChangePassword): Response<APIResponse<ResponseChangePassword>>

    suspend fun uploadAllConsentSignatures(request: RequestBody): Response<APIResponse<List<ResponseSignatureUpload>>>

    suspend fun  checkAppVersion() : Response<APIResponse<Boolean>>
    suspend fun registerPatient(hashMap: RequestBody) : Response<APIResponse<RegistrationResponse>>
    suspend fun createScreening(createRequest: RequestBody): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getNcdMRStaticData(): Response<APIResponse<NcdMRStaticDataModel>>
    suspend fun bpLogCreate(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun glucoseLogCreate(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun bpLogList(request: BPBGListModel): Response<APIResponse<BPBGListModel>>
    suspend fun glucoseLogList(request: BPBGListModel): Response<APIResponse<BPBGListModel>>
    suspend fun createAssessmentNCD(request: JsonObject): Response<HashMap<String, Any>>
    suspend fun ncdPregnancyCreate(request: PregnancyDetailsModel): Response<APIResponse<HashMap<String, Any>>>
    suspend fun ncdPregnancyDetails(request: HashMap<String, Any>): Response<APIResponse<PregnancyDetailsModel>>
    suspend fun createPatientVisit(request: PatientVisitRequest): Response<APIResponse<PatientVisitResponse>>
    suspend fun createNCDMedicalReview(request: MedicalReviewRequestResponse): Response<APIResponse<MedicalReviewResponse>>
    suspend fun fetchNCDMRSummary(request: MedicalReviewResponse): Response<APIResponse<MRSummaryResponse>>
    suspend fun createConfirmDiagonsis(request: NCDDiagnosisRequestResponse): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getConfirmDiagonsis(request: NCDDiagnosisGetRequest): Response<APIResponse<NCDDiagnosisGetResponse>>

    suspend fun createNCDPatientStatus(request: NCDPatientStatusRequest): Response<APIResponse<HashMap<String, Any>>>
    suspend fun updateNCDTreatmentPlan(request: NCDTreatmentPlanModel): Response<APIResponse<NCDTreatmentPlanModel>>
    suspend fun getNCDTreatmentPlan(request: NCDTreatmentPlanModelDetails): Response<APIResponse<NCDTreatmentPlanModelDetails>>
    suspend fun createNCDMRSummaryCreate(request: NCDMRSummaryRequestResponse): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getPrescriptionDispenseList(request: DispenseUpdateRequest): Response<APIResponse<ArrayList<DispensePrescriptionResponse>>>
    suspend fun updateDispensePrescription(request: DispensePrescriptionRequest): Response<APIResponse<DispenseUpdateResponse>>
    suspend fun getDispensePrescriptionHistory(request: DispenseUpdateRequest): Response<APIResponse<ArrayList<DispensePrescriptionResponse>>>
    suspend fun createLifestyle(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>>
    suspend fun updateLifestyle(request: AssessmentResultModel): Response<APIResponse<NCDCounselingModel>>
    suspend fun getLifestyleList(request: NCDCounselingModel): Response<APIResponse<ArrayList<NCDCounselingModel>>>
    suspend fun removeLifestyle(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>>
    suspend fun createPsychological(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>>
    suspend fun updatePsychological(request: AssessmentResultModel): Response<APIResponse<NCDCounselingModel>>
    suspend fun getPsychological(request: NCDCounselingModel): Response<APIResponse<ArrayList<NCDCounselingModel>>>
    suspend fun removePsychological(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>>
    suspend fun getPatientPrescriptionHistoryList(request: RemovePrescriptionRequest): Response<APIResponse<ArrayList<Prescription>>>
    suspend fun getPatientLabTests(request: LabTestListRequest): Response<APIResponse<ArrayList<LabTestListResponse>>>
    suspend fun getNCDMedicalReviewHistory(request: ReferralDetailRequest): Response<APIResponse<NCDMedicalReviewHistory>>
    suspend fun validatePatient(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun ncdGetInstructions(): Response<APIResponse<NCDInstructionModel>>
    suspend fun ncdUpdatePregnancyRisk(request: NCDPregnancyRiskUpdate): Response<APIResponse<Boolean>>
    suspend fun getWazWhzScore(request: WazWhzScoreRequest): Response<APIResponse<WazWhzScoreResponse>>
    suspend fun getUserDashboardDetails(request: NCDUserDashboardRequest): Response<APIResponse<NCDUserDashboardResponse>>
    suspend fun getBadgeNotifications(request: BadgeNotificationModel): Response<APIResponse<BadgeNotificationModel>>
    suspend fun updateBadgeNotifications(request: BadgeNotificationModel): Response<APIResponse<Boolean>>
    suspend fun getNcdLifeStyleDetails(request: LifeStyleRequest): Response<APIResponse<ArrayList<LifeStyleResponse>>>
    suspend fun ncdPatientRemove(request: NCDPatientRemoveRequest): Response<APIResponse<Boolean>>
    suspend fun bpLogCreateForNurse(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun glucoseLogCreateForNurse(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun ncdUpdatePatientDetail(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getUserTermsAndConditions(request: TermsAndConditionsModel): Response<APIResponse<TermsAndConditionsModel>>
    suspend fun updateTermsAndConditionsStatus(request: TermsAndConditionsModel): Response<APIResponse<TermsAndConditionsModel>>
    suspend fun ncdUpdateNextVisitDate(request: NCDMedicalReviewUpdateModel): Response<APIResponse<HashMap<String, Any>>>
    suspend fun validatePatientTransfer(request: NCDPatientTransferValidate): Response<HashMap<String, Any>>
    suspend fun createPatientTransfer(request: NCDTransferCreateRequest): Response<APIResponse<HashMap<String, Any>>>
    suspend fun searchSite(request: NCDRegionSiteModel): Response<APIResponse<ArrayList<RegionSiteResponse>>>
    suspend fun searchRoleUser(request: NCDSiteRoleModel): Response<APIResponse<ArrayList<NCDSiteRoleResponse>>>
    suspend fun getPatientListTransfer(request: NCDPatientTransferNotificationCountRequest): Response<APIResponse<PatientTransferListResponse>>
    suspend fun patientTransferNotificationCount(request: NCDPatientTransferNotificationCountRequest): Response<APIResponse<NCDPatientTransferNotificationCountResponse>>
    suspend fun patientTransferUpdate(request: NCDPatientTransferUpdateRequest): Response<APIResponse<String>>
    suspend fun getNudgesList(prescriptionNudgeRequest: PredictionRequest): Response<APIResponse<PrescriptionNudgeResponse>>
    suspend fun getLabTestNudgeList(predictionRequest: PredictionRequest): Response<APIResponse<LabTestPredictionResponse>>
    suspend fun ncdFollowUpList(request: FollowUpRequest): APIResponse<List<PatientFollowUpEntity>>
    suspend fun getPatientCallRegister(): Response<APIResponse<RegisterCallResponse>>
    suspend fun updatePatientCallRegister(request: FollowUpUpdateRequest): Response<APIResponse<HashMap<String, Any>>>
}