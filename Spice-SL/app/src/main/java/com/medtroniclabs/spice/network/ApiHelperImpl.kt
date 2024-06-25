package com.medtroniclabs.spice.network

import CreateUnderTwoMonthsRequest
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.AboveFiveYearsMetaResponse
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.AboveFiveYearsSummarySubmitRequest
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
import com.medtroniclabs.spice.data.SummarySubmitRequest
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
import com.medtroniclabs.spice.data.resource.RequestAllEntities
import com.medtroniclabs.spice.model.PatientDetailRequest
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.PatientsDataModel
import com.medtroniclabs.spice.model.ReferralData
import com.medtroniclabs.spice.model.ReferralDetailRequest
import com.medtroniclabs.spice.model.SearchAndListResponse
import com.medtroniclabs.spice.model.medicalreview.CreateUnderFiveYearsRequest
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.SummaryDetails
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

    override suspend fun getUnderTwoMonthsMetaData(): Response<APIResponse<UnderTwoMonthsMetaResponse>> {
        return apiService.getUnderTwoMonthsMetaData()
    }

    override suspend fun aboveFiveYearsSummaryCreate(request: AboveFiveYearsSummarySubmitRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.aboveFiveYearsSummaryCreate(request)
    }

    override suspend fun getLabourDeliveryMetaData(): Response<APIResponse<LabourDeliveryMetaResponse>> {
        return apiService.getLabourDeliveryMetaData()
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

    override suspend fun underTwoMonthsSummaryCreate(request: SummarySubmitRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.underTwoMonthsSummaryCreate(request)
    }


    override suspend fun saveMotherNeonateAnc(motherNeonateAncRequest: MotherNeonateAncRequest): Response<APIResponse<PatientEncounterResponse>> {
        return apiService.saveMotherNeonateAnc(motherNeonateAncRequest)
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
        return  apiService.getPrescriptionList(request)
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


    override suspend fun getPrescription(request: ReferralDetailRequest): Response<APIResponse<PrescriptionHistoryEntity>> {
        return apiService.getPrescription(request)
    }

    override suspend fun getMedicalReviewHistory(request: ReferralDetailRequest): Response<APIResponse<MedicalReviewHistory>> {
        return apiService.getMedicalReviewHistory(request)
    }
}