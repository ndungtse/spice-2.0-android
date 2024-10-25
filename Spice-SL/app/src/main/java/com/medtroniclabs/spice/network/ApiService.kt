package com.medtroniclabs.spice.network

import com.google.gson.JsonObject
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.AboveFiveYearsMetaResponse
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
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
import com.medtroniclabs.spice.data.MotherPncResponse
import com.medtroniclabs.spice.data.NCDUserDashboardRequest
import com.medtroniclabs.spice.data.NCDUserDashboardResponse
import com.medtroniclabs.spice.data.NeonatePncResponse
import com.medtroniclabs.spice.data.PatientStatusRequest
import com.medtroniclabs.spice.data.PatientStatusResponse
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.data.PrescriptionListRequest
import com.medtroniclabs.spice.data.ReferPatientAPIRequest
import com.medtroniclabs.spice.data.ReferPatientHealthFacilityItem
import com.medtroniclabs.spice.data.ReferPatientNameNumber
import com.medtroniclabs.spice.data.ReferPatientRequest
import com.medtroniclabs.spice.data.ReferPatientResult
import com.medtroniclabs.spice.data.RemovePrescriptionRequest
import com.medtroniclabs.spice.data.UnderFiveYearsMetaResponse
import com.medtroniclabs.spice.data.UnderTwoMonthsMetaResponse
import com.medtroniclabs.spice.data.UserSymptomsEntity
import com.medtroniclabs.spice.data.history.HistoryEntity
import com.medtroniclabs.spice.data.history.MedicalReviewHistory
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
import com.medtroniclabs.spice.data.model.RequestChangePassword
import com.medtroniclabs.spice.data.model.ResponseChangePassword
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.model.ResponseSignatureUpload
import com.medtroniclabs.spice.data.offlinesync.model.SyncResponse
import com.medtroniclabs.spice.data.BirthHistoryRequest
import com.medtroniclabs.spice.data.BirthHistoryResponse
import com.medtroniclabs.spice.data.MotherNeonatePncSummaryRequest
import com.medtroniclabs.spice.data.MotherNeonatePncSummaryResponse
import com.medtroniclabs.spice.data.SummaryCreateRequest
import com.medtroniclabs.spice.data.PncChildMedicalReview
import com.medtroniclabs.spice.data.PregnancyDetailsModel
import com.medtroniclabs.spice.data.history.NCDMedicalReviewHistory
import com.medtroniclabs.spice.data.model.RegistrationResponse
import com.medtroniclabs.spice.data.resource.LabourDeliverySummaryRequest
import com.medtroniclabs.spice.data.performance.CHWPerformanceMonitoring
import com.medtroniclabs.spice.data.performance.ChwVillageFilterModel
import com.medtroniclabs.spice.data.performance.FilterPreference
import com.medtroniclabs.spice.data.performance.PerformanceReportRequest
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
import com.medtroniclabs.spice.ncd.data.PatientVisitRequest
import com.medtroniclabs.spice.ncd.data.PatientVisitResponse
import com.medtroniclabs.spice.ncd.data.NCDPatientRemoveRequest
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("/auth-service/session")
    suspend fun doLogin(@Body request: RequestBody): Response<LoginResponse>

    @POST("/spice-service/static-data/user-data")
    suspend fun getMetaDataInformation(): Response<APIResponse<MetaDataResponse>>

    @POST("/spice-service/static-data/form-data")
    suspend fun getForms(@Body formRequest: FormRequest): Response<APIResponse<FormResponse>>

    @POST("/spice-service/static-data/meta-data")
    suspend fun getFormMetadata(@Body request: FormMetaRequest): Response<APIResponse<UserSymptomsEntity>>

    @POST("/offline-service/offline-sync/create")
    @JvmSuppressWildcards
    suspend fun postOfflineSyncData(@Body map: Map<String, Any>): Response<SyncResponse>

    @POST("/offline-service/offline-sync/status")
    suspend fun getOfflineSyncStatus(@Body request: RequestGetSyncStatus): Response<SyncResponse>

    @POST("/spice-service/household/list")
    suspend fun getHouseholdDetails(@Body request: RequestAllEntities): Response<APIResponse<List<HouseHold>>>

    @POST("/offline-service/offline-sync/fetch-synced-data")
    suspend fun fetchSyncedData(@Body request: RequestAllEntities): Response<ResponseBody>

    @POST("spice-service/patient/list")
    suspend fun getPatients(@Body request: PatientsDataModel): APIResponse<SearchAndListResponse>

    @POST("spice-service/patient/search")
    suspend fun patientSearch(@Body request: PatientsDataModel): APIResponse<SearchAndListResponse>

    @POST("spice-service/patient/patientDetails")
    suspend fun getPatient(@Body request: PatientDetailRequest): Response<APIResponse<PatientListRespModel>>

    @POST("/spice-service/static-data/meta-data/iccm-abovefive")
    suspend fun getAboveFiveYearsMetaData(): Response<APIResponse<AboveFiveYearsMetaResponse>>

    @POST("/spice-service/static-data/meta-data/iccm-under-five-years")
    suspend fun getUnderFiveYearsMetaData(): Response<APIResponse<UnderFiveYearsMetaResponse>>

    @POST("/spice-service/patient/referral-tickets")
    suspend fun getReferralsDetails(@Body request: ReferralDetailRequest): Response<APIResponse<ReferralData>>

    @POST("/spice-service/medical-review/iccm-general/create")
    suspend fun createAboveFiveYearsResult(@Body request: AboveFiveYearsSubmitRequest): Response<APIResponse<AboveFiveYearsSummaryDetails>>

    @POST("/spice-service/medical-review/iccm-general/details")
    suspend fun getAboveFiveYearsSummaryDetails(@Body id: AboveFiveYearsSummaryRequest): Response<APIResponse<AboveFiveYearsSummaryDetails>>

    @POST("/spice-service/static-data/meta-data/mother-neonate-anc")
    suspend fun getMotherNeoNateAncStaticData(): Response<APIResponse<MotherNeonateAncMetaResponse>>

    @POST("/spice-service/medical-review/labour-mother-neonate/create")
    suspend fun createMedicalReviewForLaborDelivery(@Body labourDeliveryRequest: CreateLabourDeliveryRequest): Response<APIResponse<CreateLabourDeliveryResponse>>

    @POST("/spice-service/static-data/meta-data/iccm-under-two-months")
    suspend fun getUnderTwoMonthsMetaData(): Response<APIResponse<UnderTwoMonthsMetaResponse>>

    @POST("/spice-service/static-data/meta-data/mother-neonate-pnc-mother")
    suspend fun getMotherPncStaticData(): Response<APIResponse<MotherPncResponse>>

    @POST("/spice-service/static-data/meta-data/mother-neonate-pnc-baby")
    suspend fun getNeonatePncStaticData(): Response<APIResponse<NeonatePncResponse>>

    @POST("/spice-service/medical-review/summary-create")
    suspend fun createSummarySubmit(@Body request: MedicalReviewSummarySubmitRequest): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/static-data/meta-data/mother-delivery")
    suspend fun getLabourDeliveryMetaData(): Response<APIResponse<LabourDeliveryMetaResponse>>

    @POST("/spice-service/medical-review/labour-mother-neonate/details")
    suspend fun getLabourDeliverySummaryDetails(@Body request: LabourDeliverySummaryDetails): Response<APIResponse<CreateLabourDeliveryRequest>>

    @POST("/admin-service/medication/search")
    suspend fun searchMedicationByName(@Body request: MedicationSearchRequest): Response<APIResponse<ArrayList<MedicationResponse>>>

    @POST("/spice-service/medical-review/iccm-under-2months/create")
    suspend fun createMedicalReviewForUnderTwoMonths(@Body request: CreateUnderTwoMonthsRequest): Response<APIResponse<CreateUnderTwoMonthsResponse>>

    @POST("/spice-service/patient/patient-status")
    suspend fun getPatientStatus(@Body request: PatientStatusRequest): Response<APIResponse<PatientStatusResponse>>

    @POST("/spice-service/medical-review/iccm-under-2months/details")
    suspend fun getUnderTwoMonthsSummaryDetails(@Body request: CreateUnderTwoMonthsResponse): Response<APIResponse<SummaryDetails>>

    @POST("/spice-service/medical-review/anc-pregnancy/create")
    suspend fun saveMotherNeonateAnc(@Body motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<PatientEncounterResponse>>

    @POST("/spice-service/medical-review/pnc/create")
    suspend fun saveMotherNeonatePnc(@Body motherNeonatePncRequest: MotherNeonatePncRequest): Response<APIResponse<PncSubmitResponse>>

    @POST("/spice-service/medical-review/anc-pregnancy/details")
    suspend fun fetchSummary(@Body motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<MotherNeonateAncSummaryModel>>

    @POST("/spice-service/medical-review/weight")
    suspend fun fetchWeight(@Body motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<BpAndWeightResponse>>

    @POST("/spice-service/medical-review/bp")
    suspend fun fetchBloodPressure(@Body motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<BpAndWeightResponse>>

    @POST("/spice-service/medical-review/weight/create")
    suspend fun createWeight(@Body bpAndWeightRequestModel: BpAndWeightRequestModel): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/medical-review/bp/create")
    suspend fun createBloodPressure(@Body bpAndWeightRequestModel: BpAndWeightRequestModel): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient/confirm-diagnosis")
    suspend fun saveUpdateDiagnosis(@Body request: DiagnosisSaveUpdateRequest): Response<APIResponse<ArrayList<DiagnosisDiseaseModel>>>

    @POST("/spice-service/patient/diagnosis-details")
    suspend fun getDiagnosisDetails(@Body request: CreateUnderTwoMonthsResponse): Response<APIResponse<ArrayList<DiagnosisDiseaseModel>>>

    @POST("/admin-service/healthfacilities-by-district-id")
    suspend fun getHealthFacilityMetaData(@Body request: ReferPatientAPIRequest): Response<APIResponse<List<ReferPatientHealthFacilityItem>>>

    @POST("/user-service/user/users-by-tenant-id")
    suspend fun getReferPatientMobileUserList(@Body tenantId: ReferPatientRequest): Response<APIResponse<List<ReferPatientNameNumber>>>

    @POST("/spice-service/patient/referral-tickets/create")
    suspend fun createReferPatientResult(@Body request: ReferPatientResult): Response<APIResponse<HashMap<String, Any>>>

    @POST("spice-service/prescription-request/create")
    suspend fun createPrescriptionRequest(@Body request: RequestBody): Response<APIResponse<Map<String, Any>>>

    @POST("spice-service/prescription-request/list")
    suspend fun getPrescriptionList(@Body request: PrescriptionListRequest): Response<APIResponse<ArrayList<Prescription>>>

    @POST("spice-service/prescription-request/remove")
    suspend fun removePrescription(@Body request: RemovePrescriptionRequest): Response<APIResponse<Map<String, Any>>>

    @POST("/spice-service/medical-review/iccm-under-5years/details")
    suspend fun getUnderFiveYearsSummaryDetails(@Body request: CreateUnderTwoMonthsResponse): Response<APIResponse<SummaryDetails>>

    @POST("/spice-service/medical-review/iccm-under-5years/create")
    suspend fun createMedicalReviewForUnderFiveYears(@Body request: CreateUnderFiveYearsRequest): Response<APIResponse<CreateUnderTwoMonthsResponse>>

    @POST("/spice-service/prescription-request/prescribed-details")
    suspend fun getPrescription(@Body request: ReferralDetailRequest): Response<APIResponse<HistoryEntity>>

    @POST("/spice-service/medical-review/history")
    suspend fun getMedicalReviewHistory(@Body request: ReferralDetailRequest): Response<APIResponse<MedicalReviewHistory>>

    @POST("/spice-service/medical-review/pnc/details")
    suspend fun getPncSummaryDetails(@Body request: MotherNeonatePncSummaryRequest): Response<APIResponse<MotherNeonatePncSummaryResponse>>

    @POST("/spice-service/medical-review/mother-neonate/summary-create")
    suspend fun summaryCreatePncData(@Body request: SummaryCreateRequest): Response<APIResponse<HashMap<String, Any>>>

    @POST("spice-service/medical-review/birth-history")
    suspend fun getBirthHistoryDetails(@Body request: BirthHistoryRequest): Response<APIResponse<BirthHistoryResponse>>

    @POST("spice-service/household/create-member")
    suspend fun addNewMember(@Body request: AddMemberRegRequest): Response<APIResponse<String>>

    @POST("/admin-service/lab-test-customization/list")
    suspend fun searchLabTestByName(@Body request: SearchRequestLabTest): Response<APIResponse<ArrayList<SearchLabTestResponse>>>

    @POST("spice-service/investigation/create")
    suspend fun createLabTest(@Body request: LabTestCreateRequest): Response<APIResponse<Map<String, Any>>>

    @POST("spice-service/investigation/list")
    suspend fun getLabTestList(@Body request: LabTestListRequest): Response<APIResponse<ArrayList<LabTestListResponse>>>

    @POST("/spice-service/investigation/remove")
    suspend fun removeLabTest(@Body request: RemoveLabTestRequest): Response<APIResponse<Map<String, Any>>>

    @POST("/spice-service/medical-review/mother-neonate/summary-create")
    suspend fun summaryCreateMotherNeonate(@Body request: LabourDeliverySummaryRequest): Response<APIResponse<HashMap<String, Any>>>
    @GET("user-service/user/peer-supervisor/linked-chw")
    suspend fun getPeerSupervisorLinkedChwList(): Response<APIResponse<List<ChwVillageFilterModel>>>

    @POST("spice-service/report/chw-performance")
    suspend fun getPeerSupervisorReport(@Body request: PerformanceReportRequest): Response<APIResponse<List<CHWPerformanceMonitoring>>>

    @POST("user-service/user/preferences")
    suspend fun getUserFilterPreference(@Body request: FilterPreference): Response<APIResponse<FilterPreference>>

    @POST("user-service/user/preferences/save")
    suspend fun saveUserFilterPreference(@Body request: FilterPreference): Response<APIResponse<FilterPreference>>


    @POST("/spice-service/medical-review/pnc-history")
    suspend fun getMedicalReviewHistoryPNC(@Body request: ReferralDetailRequest): Response<APIResponse<PncChildMedicalReview>>

    @POST("/spice-service/investigation/history-list")
    suspend fun getInvestigationHistoryList(@Body request: ReferralDetailRequest): Response<APIResponse<HistoryEntity>>

    @POST("/user-service/user/forgot-password/{email}/{client}")
    suspend fun forgotPassword(@Path("email") email: String, @Path("client") client: String): Response<APIResponse<String?>>

    @POST("/user-service/user/verify-token/{token}")
    suspend fun verifyToken(@Path("token") token: String): Response<APIResponse<String?>>

    @POST("/user-service/user/reset-password/{token}")
    suspend fun resetPassword(@Path("token") token: String, @Body request: RequestChangePassword): Response<APIResponse<ResponseChangePassword>>

    @POST("/offline-service/offline-sync/upload-signatures")
    suspend fun uploadAllConsentSignatures(@Body request: RequestBody): Response<APIResponse<List<ResponseSignatureUpload>>>

    @POST("spice-service/static-data/app-version")
    suspend fun checkAppVersion(): Response<APIResponse<Boolean>>

    @POST("/spice-service/screening/create")
    suspend fun createScreening(@Body createRequest: RequestBody): Response<APIResponse<HashMap<String, Any>>>

    @POST("spice-service/patient/register")
    suspend fun registerPatient(@Body request: RequestBody): Response<APIResponse<RegistrationResponse>>

    @POST("spice-service/static-data/ncd-medical-review")
    suspend fun getNcdMRStaticData(): Response<APIResponse<NcdMRStaticDataModel>>

    @POST("spice-service/bplog/create")
    suspend fun bpLogCreate(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("spice-service/glucoselog/create")
    suspend fun glucoseLogCreate(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("spice-service/bplog/list")
    suspend fun bpLogList(@Body request: BPBGListModel): Response<APIResponse<BPBGListModel>>

    @POST("spice-service/glucoselog/list")
    suspend fun glucoseLogList(@Body request: BPBGListModel): Response<APIResponse<BPBGListModel>>

    @POST("spice-service/assessment/create")
    suspend fun createAssessmentNCD(@Body request: JsonObject): Response<HashMap<String, Any>>

    @POST("spice-service/patient/pregnancy/create")
    suspend fun ncdPregnancyCreate(@Body request: PregnancyDetailsModel): Response<APIResponse<HashMap<String, Any>>>

    @POST("spice-service/patient/pregnancy/details")
    suspend fun ncdPregnancyDetails(@Body request: HashMap<String, Any>): Response<APIResponse<PregnancyDetailsModel>>

    @POST("/spice-service/patientvisit/create")
    suspend fun createPatientVisit(@Body request: PatientVisitRequest): Response<APIResponse<PatientVisitResponse>>

    @POST("/spice-service/medical-review/ncd/create")
    suspend fun createNCDMedicalReview(@Body request: MedicalReviewRequestResponse): Response<APIResponse<MedicalReviewResponse>>

    @POST("/spice-service/medical-review/ncd/details")
    suspend fun fetchNCDMRSummary(@Body request: MedicalReviewResponse): Response<APIResponse<MRSummaryResponse>>

    @POST("spice-service/prescription-request/history-list")
    suspend fun getPatientPrescriptionHistoryList(@Body request: RemovePrescriptionRequest) : Response<APIResponse<ArrayList<Prescription>>>

    @POST("/spice-service/medical-review/confirm-diagnosis/update")
    suspend fun createConfirmDiagonsis(@Body request: NCDDiagnosisRequestResponse): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient/get-diagnosis-details")
    suspend fun getConfirmDiagonsis(@Body request: NCDDiagnosisGetRequest): Response<APIResponse<NCDDiagnosisGetResponse>>

    @POST("/spice-service/medical-review/patient-status/create")
    suspend fun createNCDPatientStatus(@Body request: NCDPatientStatusRequest): Response<APIResponse<HashMap<String, Any>>>
    @POST("/spice-service/patient-treatment-plan/update")
    suspend fun updateNCDTreatmentPlan(@Body request: NCDTreatmentPlanModel): Response<APIResponse<NCDTreatmentPlanModel>>

    @POST("/spice-service/patient-treatment-plan/details")
    suspend fun getNCDTreatmentPlan(@Body request: NCDTreatmentPlanModelDetails): Response<APIResponse<NCDTreatmentPlanModelDetails>>

    @POST("/spice-service/medical-review/ncd/summary-create")
    suspend fun createNCDMRSummaryCreate(@Body request: NCDMRSummaryRequestResponse): Response<APIResponse<HashMap<String, Any>>>

    @POST("spice-service/patient-nutrition-lifestyle/create")
    suspend fun createLifestyle(@Body request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>>

    @PUT("spice-service/patient-nutrition-lifestyle/update")
    suspend fun updateLifestyle(@Body request: AssessmentResultModel): Response<APIResponse<NCDCounselingModel>>

    @POST("spice-service/patient-nutrition-lifestyle/list")
    suspend fun getLifestyleList(@Body request: NCDCounselingModel): Response<APIResponse<ArrayList<NCDCounselingModel>>>

    @POST("spice-service/patient-nutrition-lifestyle/remove")
    suspend fun removeLifestyle(@Body request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>>

    @POST("spice-service/medical-review/patient-psychology/create")
    suspend fun createPsychological(@Body request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>>

    @PUT("spice-service/medical-review/patient-psychology/update")
    suspend fun updatePsychological(@Body request: AssessmentResultModel): Response<APIResponse<NCDCounselingModel>>

    @POST("spice-service/medical-review/patient-psychology/list")
    suspend fun getPsychological(@Body request: NCDCounselingModel): Response<APIResponse<ArrayList<NCDCounselingModel>>>

    @POST("spice-service/medical-review/patient-psychology/remove")
    suspend fun removePsychological(@Body request: NCDCounselingModel): Response<APIResponse<NCDCounselingModel>>

    @POST("spice-service/investigation/list")
    suspend fun getPatientLabTests(@Body request: LabTestListRequest): Response<APIResponse<ArrayList<LabTestListResponse>>>

    @POST("/spice-service/prescription-request/fill-prescription/list")
    suspend fun getPrescriptionDispenseList(@Body request: DispenseUpdateRequest): Response<APIResponse<ArrayList<DispensePrescriptionResponse>>>

    @POST("/spice-service/prescription-request/fill-prescription/update")
    suspend fun updateDispensePrescription(@Body request: DispensePrescriptionRequest):  Response<APIResponse<DispenseUpdateResponse>>

    @POST("/spice-service/prescription-request/refill-prescription/history")
    suspend fun getDispensePrescriptionHistory(@Body request: DispenseUpdateRequest): Response<APIResponse<ArrayList<DispensePrescriptionResponse>>>


    @POST("/spice-service/medical-review/ncd/history-list")
    suspend fun getNCDMedicalReviewHistory(@Body request: ReferralDetailRequest): Response<APIResponse<NCDMedicalReviewHistory>>

    @POST("spice-service/patient/validate")
    suspend fun validatePatient(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @GET("/spice-service/medical-review/get-instructions")
    suspend fun ncdGetInstructions(): Response<APIResponse<NCDInstructionModel>>

    @PUT("/spice-service/patient/pregnancy-anc-risk/update")
    suspend fun ncdUpdatePregnancyRisk(@Body request: NCDPregnancyRiskUpdate): Response<APIResponse<Boolean>>

    @POST("/spice-service/screening/dashboard-count")
    suspend fun getUserDashboardDetails(@Body request: NCDUserDashboardRequest): Response<APIResponse<NCDUserDashboardResponse>>

    @POST("spice-service/medical-review/count")
    suspend fun getBadgeNotifications(@Body request: BadgeNotificationModel): Response<APIResponse<BadgeNotificationModel>>

    @PUT("spice-service/medical-review/update-view-status")
    suspend fun updateBadgeNotifications(@Body request: BadgeNotificationModel): Response<APIResponse<Boolean>>

    @POST("/spice-service/medical-review/patient-lifestyle-details")
    suspend fun getNcdLifeStyleDetails(@Body request: LifeStyleRequest): Response<APIResponse<ArrayList<LifeStyleResponse>>>


    @POST("/spice-service/patient/delete")
    suspend fun ncdPatientRemove(@Body request: NCDPatientRemoveRequest): Response<APIResponse<Boolean>>
}