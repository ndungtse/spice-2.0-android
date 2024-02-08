package com.medtroniclabs.spice.network

import com.medtroniclabs.spice.data.LoginResponse
import okhttp3.MultipartBody
import retrofit2.Response

interface ApiHelper {
    suspend fun doLogin(loginRequest: MultipartBody): Response<LoginResponse>
}