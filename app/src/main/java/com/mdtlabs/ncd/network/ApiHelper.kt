package com.mdtlabs.ncd.network

import com.mdtlabs.ncd.data.model.CultureModel
import com.mdtlabs.ncd.data.model.LoginRequest
import com.mdtlabs.ncd.data.model.LoginResponse
import retrofit2.Response

interface ApiHelper {
    suspend fun getCulture(): Response<CultureModel>
    suspend fun getTranslation(): Response<HashMap<*, *>>
    suspend fun doLogin(loginRequest: LoginRequest): Response<LoginResponse>
}