package com.medtroniclabs.spice.common

import com.medtroniclabs.spice.BuildConfig
import com.medtroniclabs.spice.data.model.BaseUrlRequest
import com.medtroniclabs.spice.data.model.BaseUrlResponse
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

object BaseUrlProvider {

    const val BaseUrl = "https://su.medtroniclabs.org/sl/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    interface DynamicBaseUrlApiService {

        @POST("get-base-url")
        suspend fun getBaseUrl(@retrofit2.http.Body request: BaseUrlRequest): BaseUrlResponse

    }

    private suspend fun fetchBaseUrl(): String {
        val service = retrofit.create(DynamicBaseUrlApiService::class.java)
        val request = BaseUrlRequest(
            versionCode = BuildConfig.VERSION_CODE.toString(),
            appVersion = BuildConfig.VERSION_NAME,
            deviceId = SecuredPreference.getDeviceId()
        )
        return try {
            service.getBaseUrl(request).url ?: BuildConfig.API_BASE_URL
        } catch (e: Exception) {
            e.printStackTrace()
            BuildConfig.API_BASE_URL
        }
    }


    fun dynamicURL(): String {
        var baseUrl: String = BuildConfig.API_BASE_URL
        runBlocking {
            baseUrl = fetchBaseUrl()
        }
        return baseUrl
    }

}