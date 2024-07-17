package com.medtroniclabs.spice.network

import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsRequest
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.AboveFiveYearsMetaResponse
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.MedicalReviewSummarySubmitRequest
import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.DiagnosisSaveUpdateRequest
import com.medtroniclabs.spice.data.FormMetaRequest
import com.medtroniclabs.spice.data.FormRequest
import com.medtroniclabs.spice.data.FormResponse
import com.medtroniclabs.spice.data.LabourDeliveryMetaResponse
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.MedicationResponse
import com.medtroniclabs.spice.data.MedicationSearchRequest
import com.medtroniclabs.spice.data.MetaDataResponse
import com.medtroniclabs.spice.data.MotherNeonateAncMetaResponse
import com.medtroniclabs.spice.data.MotherNeonateAncSummaryModel
import com.medtroniclabs.spice.data.PatientStatusRequest
import com.medtroniclabs.spice.data.PatientStatusResponse
import com.medtroniclabs.spice.data.PrescriptionListRequest
import com.medtroniclabs.spice.data.ReferPatientAPIRequest
import com.medtroniclabs.spice.data.ReferPatientHealthFacilityItem
import com.medtroniclabs.spice.data.ReferPatientNameNumber
import com.medtroniclabs.spice.data.ReferPatientRequest
import com.medtroniclabs.spice.data.ReferPatientResult
import com.medtroniclabs.spice.data.UnderFiveYearsMetaResponse
import com.medtroniclabs.spice.data.UnderTwoMonthsMetaResponse
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.data.RemovePrescriptionRequest
import com.medtroniclabs.spice.data.UserSymptomsEntity
import com.medtroniclabs.spice.data.history.MedicalReviewHistory
import com.medtroniclabs.spice.data.history.PrescriptionHistoryEntity
import com.medtroniclabs.spice.data.model.AboveFiveYearsSubmitRequest
import com.medtroniclabs.spice.data.model.BpAndWeightRequestModel
import com.medtroniclabs.spice.data.model.BpAndWeightResponse
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.PatientEncounterResponse
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.model.SyncResponse
import com.medtroniclabs.spice.data.BirthHistoryRequest
import com.medtroniclabs.spice.data.BirthHistoryResponse
import com.medtroniclabs.spice.data.resource.RequestAllEntities
import com.medtroniclabs.spice.model.PatientDetailRequest
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.PatientsDataModel
import com.medtroniclabs.spice.model.ReferralData
import com.medtroniclabs.spice.model.ReferralDetailRequest
import com.medtroniclabs.spice.model.SearchAndListResponse
import com.medtroniclabs.spice.model.medicalreview.CreateUnderFiveYearsRequest
import com.medtroniclabs.spice.model.medicalreview.CreateLabourDeliveryRequest
import com.medtroniclabs.spice.model.medicalreview.CreateLabourDeliveryResponse
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.LabourDeliverySummaryDetails
import com.medtroniclabs.spice.model.medicalreview.SummaryDetails
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

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
    suspend fun getUnderTwoMonthsSummaryDetails(@Body request : CreateUnderTwoMonthsResponse ): Response<APIResponse<SummaryDetails>>

    @POST("/spice-service/medical-review/anc-pregnancy/create")
    suspend fun saveMotherNeonateAnc(@Body motherNeonateAncRequest: MotherNeonateAncRequest):Response<APIResponse<PatientEncounterResponse>>

    @POST("/spice-service/medical-review/anc-pregnancy/details")
    suspend fun fetchSummary(@Body motherNeonateAncRequest : MotherNeonateAncRequest) : Response<APIResponse<MotherNeonateAncSummaryModel>>

    @POST("/spice-service/medical-review/weight")
    suspend fun fetchWeight(@Body motherNeonateAncRequest :MotherNeonateAncRequest):Response<APIResponse<BpAndWeightResponse>>

    @POST("/spice-service/medical-review/bp")
    suspend fun fetchBloodPressure(@Body motherNeonateAncRequest :MotherNeonateAncRequest):Response<APIResponse<BpAndWeightResponse>>

    @POST("/spice-service/medical-review/weight/create")
    suspend fun createWeight(@Body bpAndWeightRequestModel : BpAndWeightRequestModel) : Response<APIResponse<HashMap<String,Any>>>

    @POST("/spice-service/medical-review/bp/create")
    suspend fun createBloodPressure(@Body bpAndWeightRequestModel :BpAndWeightRequestModel) : Response<APIResponse<HashMap<String,Any>>>

    @POST("/spice-service/patient/confirm-diagnosis")
    suspend fun saveUpdateDiagnosis(@Body request: DiagnosisSaveUpdateRequest):Response<APIResponse<ArrayList<DiagnosisDiseaseModel>>>

    @POST("/spice-service/patient/diagnosis-details")
    suspend fun getDiagnosisDetails(@Body request: CreateUnderTwoMonthsResponse):Response<APIResponse<ArrayList<DiagnosisDiseaseModel>>>

    @POST("/admin-service/healthfacilities-by-district-id")
    suspend fun getHealthFacilityMetaData(@Body request: ReferPatientAPIRequest): Response<APIResponse<List<ReferPatientHealthFacilityItem>>>

    @POST("/user-service/user/users-by-tenant-id")
    suspend fun getReferPatientMobileUserList(@Body tenantId: ReferPatientRequest): Response<APIResponse<List<ReferPatientNameNumber>>>

    @POST("/spice-service/patient/referral-tickets/create")
    suspend fun createReferPatientResult(@Body request: ReferPatientResult): Response<APIResponse<HashMap<String,Any>>>

    @POST("spice-service/prescription-request/create")
    suspend fun createPrescriptionRequest(@Body request: RequestBody):Response<APIResponse<Map<String,Any>>>

    @POST("spice-service/prescription-request/list")
    suspend fun getPrescriptionList(@Body request: PrescriptionListRequest):Response<APIResponse<ArrayList<Prescription>>>

    @POST("spice-service/prescription-request/remove")
    suspend fun removePrescription(@Body request: RemovePrescriptionRequest):Response<APIResponse<Map<String,Any>>>

    @POST("/spice-service/medical-review/iccm-under-5years/details")
    suspend fun getUnderFiveYearsSummaryDetails(@Body request : CreateUnderTwoMonthsResponse ): Response<APIResponse<SummaryDetails>>

    @POST("/spice-service/medical-review/iccm-under-5years/create")
    suspend fun createMedicalReviewForUnderFiveYears(@Body request: CreateUnderFiveYearsRequest): Response<APIResponse<CreateUnderTwoMonthsResponse>>

    @POST("/spice-service/prescription-request/prescribed-details")
    suspend fun getPrescription(@Body request: ReferralDetailRequest): Response<APIResponse<PrescriptionHistoryEntity>>

    @POST("/spice-service/medical-review/history")
    suspend fun getMedicalReviewHistory(@Body request: ReferralDetailRequest): Response<APIResponse<MedicalReviewHistory>>

    @POST("spice-service/medical-review/birth-history")
    suspend fun getBirthHistoryDetails(@Body request: BirthHistoryRequest): Response<APIResponse<BirthHistoryResponse>>
}