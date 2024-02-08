package com.medtroniclabs.spice.network

import com.medtroniclabs.spice.data.LoginResponse
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(private val apiService: ApiService) : ApiHelper {
    override suspend fun doLogin(loginRequest: MultipartBody): Response<LoginResponse> {
        return apiService.doLogin(loginRequest)
    }

}