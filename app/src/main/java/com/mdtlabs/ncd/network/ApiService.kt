package com.mdtlabs.ncd.network

import com.mdtlabs.ncd.data.model.CultureModel
import com.mdtlabs.ncd.data.model.LoginRequest
import com.mdtlabs.ncd.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("/culture")
    suspend fun getCulture(): Response<CultureModel>

    @GET("/translation")
    suspend fun getTranslations(): Response<HashMap<*, *>>

    @POST("/login")
    suspend fun doLogin(@Body loginRequest: LoginRequest): Response<LoginResponse>

}
