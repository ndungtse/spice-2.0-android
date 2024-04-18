package com.medtroniclabs.spice.network

import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.AboveFiveYearsMetaResponse
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.AboveFiveYearsSummarySubmitRequest
import com.medtroniclabs.spice.data.FormMetaRequest
import com.medtroniclabs.spice.data.FormRequest
import com.medtroniclabs.spice.data.FormResponse
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.MetaDataResponse
import com.medtroniclabs.spice.data.MotherNeonateAncMetaResponse
import com.medtroniclabs.spice.data.UnderTwoMonthsMetaResponse
import com.medtroniclabs.spice.data.UserSymptomsEntity
import com.medtroniclabs.spice.data.model.AboveFiveYearsSubmitRequest
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.model.ResponseInitialDownload
import com.medtroniclabs.spice.data.offlinesync.model.SyncResponse
import com.medtroniclabs.spice.data.resource.RequestAllEntities
import com.medtroniclabs.spice.model.PatientDetailRequest
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.PatientsDataModel
import com.medtroniclabs.spice.model.ReferralData
import okhttp3.MultipartBody
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

    override suspend fun getPatients(request: PatientsDataModel): APIResponse<List<PatientListRespModel>> {
        return apiService.getPatients(request)
    }
    override suspend fun postOfflineSync(request: Map<String, Any>): Response<SyncResponse> {
        return apiService.postOfflineSyncData(request)
    }

    override suspend fun getOfflineSyncStatus(request: RequestGetSyncStatus): Response<SyncResponse> {
        return apiService.getOfflineSyncStatus(request)
    }

    override suspend fun fetchSyncedData(request: RequestAllEntities): Response<APIResponse<ResponseInitialDownload>> {
        return apiService.fetchSyncedData(request)
    }

    override suspend fun getHouseholdAndMembers(request: RequestAllEntities): Response<APIResponse<List<HouseHold>>> {
        return apiService.getHouseholdDetails(request)
    }

    override suspend fun patientSearch(request: PatientsDataModel): APIResponse<List<PatientListRespModel>> {
        return apiService.patientSearch(request)
    }

    override suspend fun getPatient(request: PatientDetailRequest): Response<APIResponse<PatientListRespModel>> {
        return apiService.getPatient(request)
    }

    override suspend fun getAboveFiveYearsMetaData(): Response<APIResponse<AboveFiveYearsMetaResponse>> {
        return apiService.getAboveFiveYearsMetaData()
    }

    override suspend fun getReferralsDetails(request: PatientDetailRequest): Response<APIResponse<ReferralData>> {
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

    override suspend fun aboveFiveYearsSummaryCreate(request: AboveFiveYearsSummarySubmitRequest): Response<APIResponse<HashMap<String,Any>>> {
        return apiService.aboveFiveYearsSummaryCreate(request)
    }

}