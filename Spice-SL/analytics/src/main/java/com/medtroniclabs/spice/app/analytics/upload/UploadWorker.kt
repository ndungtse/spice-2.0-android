package com.medtroniclabs.spice.app.analytics.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.medtroniclabs.spice.app.analytics.db.Analytics
import com.medtroniclabs.spice.app.analytics.db.AnalyticsRepository
import com.medtroniclabs.spice.app.analytics.model.AnalyticsData
import com.medtroniclabs.spice.app.analytics.model.AnalyticsDetail
import com.medtroniclabs.spice.app.analytics.network.ApiService
import com.medtroniclabs.spice.app.analytics.network.RetrofitHelper
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils
import com.medtroniclabs.spice.app.analytics.utils.UserDetail
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val repository: AnalyticsRepository = AnalyticsRepository(context)

    private var stopIteration = false

    override suspend fun doWork(): Result {
        val headers = inputData.keyValueMap
        val baseUrl = inputData.getString(AnalyticsDefinedParams.BaseUrl)
        val lastSyncDate = inputData.getString(AnalyticsDefinedParams.LastSyncDate)
        generateAnalyticsReport(
            baseUrl,
            headers[AnalyticsDefinedParams.Authorization].toString(),
            lastSyncDate ?: "--"
        )
        return Result.success()
    }

    private suspend fun getAllAnalyticsData(lastSyncDate: String): AnalyticsData {
        val list = repository.getAllAnalytics()
        // Group by userId and then by eventType, converting parameter to Json by UserID
        val userAnalyticsMap = list.groupBy(Analytics::userId)
            .mapValues { (_, analyticsList) ->
                analyticsList.groupBy(Analytics::eventType)
                    .mapValues { (_, eventList) ->
                        eventList.map { JsonParser.parseString(it.parameter) }.toMutableList()
                    }.toMutableMap()
            }

        // Convert the map to a list of AnalyticsDetail
        val userAnalyticsList = userAnalyticsMap.map { (userId, analyticsMap) ->
            AnalyticsDetail(id = userId, lastSyncDate, analytics = analyticsMap)
        }

        return AnalyticsData(userAnalytics = userAnalyticsList)
    }


    // Generating & Saving the json file of analytics data in local
    private suspend fun generateAnalyticsReport(
        baseUrl: String?,
        headers: Any?,
        lastSyncDate: String
    ) {
        val analyticsData = getAllAnalyticsData(lastSyncDate)

        try {
            applicationContext.openFileOutput(
                CommonUtils.getAnalyticsFileName(),
                Context.MODE_PRIVATE
            ).use { outputStream ->
                outputStream.write(Gson().toJson(analyticsData).toByteArray())
            }

            baseUrl?.let {
                uploadFileFilter(it, headers.toString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // While extracting file from local Directory filtering based on userid
    private suspend fun uploadFileFilter(baseUrl: String, headers: String) {
        val filteredList = listFilesInDirectoryWithFilter(UserDetail.userId)
        filteredList.forEach {
            if (!stopIteration) {
                uploadFileApiCall(baseUrl, headers, it)
            }
        }

    }


    private suspend fun uploadFileApiCall(
        baseUrl: String, header: String, file: File
    ) {
        val filePart = prepareFilePart(file)
        val apiService = initializeApiService(baseUrl, header)
        try {
            retryApiCall(3,
                apiCall = {
                    val response = apiService.uploadFile(filePart)
                    if (response.isSuccessful) {
//                        deleteUploadedFiles(file.name)
                        true
                    } else {
                        false
                    }
                },
                onRetry = {
                    // Handle the case on retry attempt
                },
                onFail = {
                    stopIteration = true
                    // Handle the case where all retry attempts failed
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private suspend fun retryApiCall(
        maxRetries: Int,
        apiCall: suspend () -> Boolean,
        onRetry: (retryCount: Int) -> Unit,
        onFail: () -> Unit
    ) {
        var retry = 0
        var success = false
        while (retry < maxRetries && !success) {
            if (apiCall()) {
                success = true
            } else {
                retry++
                onRetry(retry)
            }
        }
        if (retry == maxRetries) {
            onFail()
        }
    }


    private fun prepareFilePart(file: File): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            "file", file.name, file.asRequestBody("application/json".toMediaTypeOrNull())
        )
    }

    private fun initializeApiService(baseUrl: String, header: String): ApiService {
        return RetrofitHelper.initializeRetrofit(baseUrl, header)
    }


    private fun deleteUploadedFiles(filterString: String) {
        listFilesInDirectoryWithFilter(filterString).forEach { fileName ->
            fileName.delete()
        }
    }


    private fun listFilesInDirectoryWithFilter(
        filterString: String
    ): List<File> {
        val directoryPath = applicationContext.filesDir.absolutePath
        val directory = File(directoryPath)
        return directory.listFiles { file -> file.isFile && file.name.contains(filterString) }
            ?.map { it } ?: emptyList()
    }
}