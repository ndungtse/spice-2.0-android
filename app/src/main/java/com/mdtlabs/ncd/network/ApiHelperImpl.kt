package com.mdtlabs.ncd.network

import com.mdtlabs.ncd.data.model.CultureModel
import com.mdtlabs.ncd.data.model.LoginRequest
import com.mdtlabs.ncd.data.model.LoginResponse
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl
@Inject
constructor(private val apiService: ApiService) : ApiHelper {
    override suspend fun getCulture(): Response<CultureModel> {
       return apiService.getCulture()
    }

    override suspend fun getTranslation(): Response<HashMap<*,*>> {
        return apiService.getTranslations()
    }

    override suspend fun doLogin(loginRequest: LoginRequest): Response<LoginResponse> {
        return apiService.doLogin(loginRequest)
    }

}