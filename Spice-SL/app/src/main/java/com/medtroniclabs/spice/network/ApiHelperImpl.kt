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
import com.medtroniclabs.spice.ncd.data.PatientVisitRequest
import com.medtroniclabs.spice.ncd.data.PatientVisitResponse
import com.medtroniclabs.spice.ncd.data.ValidatePatientModel
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

    override suspend fun updateLifestyle(request: AssessmentResultModel): Response<APIResponse<NCDCounselingModel>> {
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

    override suspend fun updatePsychological(request: AssessmentResultModel): Response<APIResponse<NCDCounselingModel>> {
        return apiService.updatePsychological(request)
    }

    override suspend fun getPsychological(request: NCDCounselingModel): Response<APIResponse<ArrayList<NCDCounselingModel>>> {
        return apiService.getPsychological(request)
    }

    override suspend fun removePsychological(request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>> {
        return apiService.removePsychological(request)
    }

    override suspend fun getPatientLabTests(request: LabTestListRequest): Response<APIResponse<ArrayList<LabTestListResponse>>> {
        return apiService.getPatientLabTests(request)
    }

    override suspend fun getNCDMedicalReviewHistory(request: ReferralDetailRequest): Response<APIResponse<NCDMedicalReviewHistory>> {
        return apiService.getNCDMedicalReviewHistory(request)
    }

    override suspend fun validatePatient(request: ValidatePatientModel): Response<APIResponse<ValidatePatientModel>> {
        return apiService.validatePatient(request)
    }

    override suspend fun ncdGetInstructions(): Response<APIResponse<NCDInstructionModel>> {
        return apiService.ncdGetInstructions()
    }

    override suspend fun ncdUpdatePregnancyRisk(request: NCDPregnancyRiskUpdate): Response<APIResponse<Boolean>> {
        return apiService.ncdUpdatePregnancyRisk(request)
    }

    override suspend fun getUserDashboardDetails(request: NCDUserDashboardRequest): Response<APIResponse<NCDUserDashboardResponse>> {
        return apiService.getUserDashboardDetails(request)
    }
}