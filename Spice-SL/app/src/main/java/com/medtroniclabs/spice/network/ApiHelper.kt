package com.medtroniclabs.spice.network

import com.medtroniclabs.spice.data.model.MotherNeonatePncRequest
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.AboveFiveYearsMetaResponse
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.BirthHistoryRequest
import com.medtroniclabs.spice.data.BirthHistoryResponse
import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.DiagnosisSaveUpdateRequest
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
import com.medtroniclabs.spice.data.NeonatePncResponse
import com.medtroniclabs.spice.data.PatientStatusRequest
import com.medtroniclabs.spice.data.PatientStatusResponse
import com.medtroniclabs.spice.data.PncChildMedicalReview
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
import com.medtroniclabs.spice.data.history.MedicalReviewHistory
import com.medtroniclabs.spice.data.history.HistoryEntity
import com.medtroniclabs.spice.data.model.AboveFiveYearsSubmitRequest
import com.medtroniclabs.spice.data.model.BpAndWeightRequestModel
import com.medtroniclabs.spice.data.model.BpAndWeightResponse
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.PatientEncounterResponse
import com.medtroniclabs.spice.data.model.PncSubmitResponse
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.model.SyncResponse
import com.medtroniclabs.spice.data.resource.LabourDeliverySummaryRequest
import com.medtroniclabs.spice.data.performance.CHWPerformanceMonitoring
import com.medtroniclabs.spice.data.performance.ChwVillageFilterModel
import com.medtroniclabs.spice.data.performance.FilterPreference
import com.medtroniclabs.spice.data.performance.PerformanceReportRequest
import com.medtroniclabs.spice.data.resource.RequestAllEntities
import com.medtroniclabs.spice.model.LabTestCreateRequest
import com.medtroniclabs.spice.model.LabTestListRequest
import com.medtroniclabs.spice.model.LabTestListResponse
import com.medtroniclabs.spice.model.PatientDetailRequest
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.PatientsDataModel
import com.medtroniclabs.spice.model.ReferralData
import com.medtroniclabs.spice.model.ReferralDetailRequest
import com.medtroniclabs.spice.model.RemoveLabTestRequest
import com.medtroniclabs.spice.model.SearchAndListResponse
import com.medtroniclabs.spice.model.medicalreview.AddMemberRegRequest
import com.medtroniclabs.spice.data.model.CreateLabourDeliveryRequest
import com.medtroniclabs.spice.data.model.CreateLabourDeliveryResponse
import com.medtroniclabs.spice.model.medicalreview.CreateUnderFiveYearsRequest
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsRequest
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.data.model.LabourDeliverySummaryDetails
import com.medtroniclabs.spice.data.model.RequestChangePassword
import com.medtroniclabs.spice.data.model.ResponseChangePassword
import com.medtroniclabs.spice.data.offlinesync.model.ResponseSignatureUpload
import com.medtroniclabs.spice.model.medicalreview.SearchLabTestResponse
import com.medtroniclabs.spice.model.medicalreview.SearchRequestLabTest
import com.medtroniclabs.spice.model.medicalreview.SummaryDetails
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
}