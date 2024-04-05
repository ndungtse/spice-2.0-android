package com.medtroniclabs.spice.network

import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.AboveFiveYearsMetaResponse
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.FormMetaRequest
import com.medtroniclabs.spice.data.FormRequest
import com.medtroniclabs.spice.data.FormResponse
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.MetaDataResponse
import com.medtroniclabs.spice.data.UserSymptomsEntity
import com.medtroniclabs.spice.data.model.AboveFiveYearsSubmitRequest
import com.medtroniclabs.spice.data.offlinesync.model.HouseHold
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.model.ResponseInitialDownload
import com.medtroniclabs.spice.data.offlinesync.model.SyncResponse
import com.medtroniclabs.spice.data.resource.RequestAllEntities
import com.medtroniclabs.spice.model.PatientDetailReq
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.PatientsDataModel
import com.medtroniclabs.spice.model.ReferralData
import okhttp3.RequestBody
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
    suspend fun fetchSyncedData(@Body request: RequestAllEntities): Response<APIResponse<ResponseInitialDownload>>


    @POST("spice-service/patient/list")
    suspend fun getPatients(@Body request: PatientsDataModel): APIResponse<List<PatientListRespModel>>

    @POST("spice-service/patient/search")
    suspend fun patientSearch(@Body request: PatientsDataModel): APIResponse<List<PatientListRespModel>>

    @POST("spice-service/patient/patientDetails")
    suspend fun getPatient(@Body request: PatientDetailReq): Response<APIResponse<PatientListRespModel>>

    @POST("/spice-service/static-data/meta-data/iccm-abovefive")
    suspend fun getAboveFiveYearsMetaData(): Response<APIResponse<AboveFiveYearsMetaResponse>>

    @POST("/spice-service/assessment/referral-tickets")
    suspend fun getReferralsDetails(@Body request: PatientDetailReq): Response<APIResponse<ReferralData>>

    @POST("/spice-service/medical-review/iccm-general/create")
    suspend fun createAboveFiveYearsResult(@Body request: AboveFiveYearsSubmitRequest): Response<APIResponse<AboveFiveYearsSummaryDetails>>

    @POST("/spice-service/medical-review/iccm-general/details")
    suspend fun getAboveFiveYearsSummaryDetails(@Body id: AboveFiveYearsSummaryRequest): Response<APIResponse<AboveFiveYearsSummaryDetails>>

}