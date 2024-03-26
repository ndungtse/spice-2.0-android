package com.medtroniclabs.spice.network

import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.FormMetaRequest
import com.medtroniclabs.spice.data.FormRequest
import com.medtroniclabs.spice.data.FormResponse
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.MetaDataResponse
import com.medtroniclabs.spice.data.UserSymptomsEntity
import com.medtroniclabs.spice.model.resource.RequestAllEntities
import com.medtroniclabs.spice.offlinesync.model.HouseHold
import com.medtroniclabs.spice.model.PatientDetailReq
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.PatientsDataModel
import com.medtroniclabs.spice.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.offlinesync.model.SyncResponse
import okhttp3.MultipartBody
import retrofit2.Response

interface ApiHelper {
    suspend fun doLogin(loginRequest: MultipartBody): Response<LoginResponse>
    suspend fun getMetaDataInformation(): Response<APIResponse<MetaDataResponse>>
    suspend fun getForms(formRequest: FormRequest): Response<APIResponse<FormResponse>>
    suspend fun getFormMetadata(request: FormMetaRequest): Response<APIResponse<UserSymptomsEntity>>
    suspend fun postOfflineSync(request: Map<String,Any>): Response<SyncResponse>
    suspend fun getOfflineSyncStatus(request: RequestGetSyncStatus): Response<SyncResponse>
    suspend fun getHouseholdAndMembers(request: RequestAllEntities): Response<APIResponse<List<HouseHold>>>
    suspend fun getPatients(request: PatientsDataModel): APIResponse<List<PatientListRespModel>>
    suspend fun patientSearch(request: PatientsDataModel): APIResponse<List<PatientListRespModel>>
    suspend fun getPatient(request: PatientDetailReq): Response<APIResponse<PatientListRespModel>>
}