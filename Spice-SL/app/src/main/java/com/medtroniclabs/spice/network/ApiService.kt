package com.medtroniclabs.spice.network

import com.medtroniclabs.spice.data.LoginResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/auth-service/session")
    suspend fun doLogin(@Body request: RequestBody): Response<LoginResponse>
}