package com.medtroniclabs.spice.network

import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.data.FormMetaRequest
import com.medtroniclabs.spice.data.FormRequest
import com.medtroniclabs.spice.data.FormResponse
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.MetaDataResponse
import com.medtroniclabs.spice.data.UserSymptomsEntity
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
}