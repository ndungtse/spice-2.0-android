package com.medtroniclabs.spice.app.analytics.upload

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.medtroniclabs.spice.app.analytics.db.AnalyticsRepository
import com.medtroniclabs.spice.app.analytics.model.Analytics
import com.medtroniclabs.spice.app.analytics.model.AnalyticsData
import com.medtroniclabs.spice.app.analytics.model.AnalyticsDetail
import com.medtroniclabs.spice.app.analytics.network.ApiService
import com.medtroniclabs.spice.app.analytics.network.RetrofitHelper
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val repository: AnalyticsRepository = AnalyticsRepository(context)

    private var stopIteration = false

    override suspend fun doWork(): Result {
        Log.i("Analytics","UploadingLogs")
        val headers = inputData.keyValueMap
        val baseUrl = inputData.getString(AnalyticsDefinedParams.BaseUrl)
        val buildConfig= inputData.getString(AnalyticsDefinedParams.BuildConfig)
        stopIteration = false
        generateAnalyticsReport(
            baseUrl,
            headers[AnalyticsDefinedParams.Authorization].toString(),
            buildConfig
        )
        return Result.success()
    }

    private suspend fun getAllAnalyticsData(): AnalyticsData? {
        val list = repository.getAllAnalytics()
        val userAnalyticsMap = list.groupBy(Analytics::userId)
            .mapValues { (_, analyticsList) ->
                val lastSyncDate =
                    analyticsList.maxByOrNull { it.lastSyncDate ?: "--" }?.lastSyncDate ?: "--"
                val role =
                    analyticsList.first().role  // Assuming role is consistent within a userId group
                val analyticsMap = analyticsList.groupBy(Analytics::eventType)
                    .mapValues { (_, eventList) ->
                        eventList.map { JsonParser.parseString(it.parameter) }.toMutableList()
                    }.toMutableMap()
                val sessionTracking =
                    analyticsList.filter { it.eventType == AnalyticsDefinedParams.SessionTracking }
                        .map { JsonParser.parseString(it.parameter) }.toMutableList()
                analyticsMap.remove(AnalyticsDefinedParams.SessionTracking)
                Triple(analyticsMap, lastSyncDate, sessionTracking to role)
            }
        val userAnalyticsList = userAnalyticsMap.map { (userId, analyticsTriple) ->
            val (analyticsMap, lastSyncDate, sessionTrackingRolePair) = analyticsTriple
            val (sessionTracking, role) = sessionTrackingRolePair
            AnalyticsDetail(
                id = userId,
                role = role,
                lastSyncDate = lastSyncDate,
                analytics = analyticsMap,
                sessionTracking = sessionTracking
            )
        }

        return if (userAnalyticsList.isNotEmpty())
            AnalyticsData(userAnalytics = userAnalyticsList)
        else
            null
    }

    // Generating & Saving the json file of analytics data in local
    private suspend fun generateAnalyticsReport(
        baseUrl: String?,
        headers: Any?,
        buildConfig: String?,
    ) {

        getAllAnalyticsData()?.let { analyticsData ->
            try {
                val ids = analyticsData.userAnalytics.joinToString(separator = "_") { it.id }
                val fileName = CommonUtils.getAnalyticsFileName(ids)
                applicationContext.openFileOutput(
                    fileName,
                    Context.MODE_PRIVATE
                ).use { outputStream ->
                    outputStream.write(Gson().toJson(analyticsData).toByteArray())
                }

                Log.i("Analytics","Base URL : "+baseUrl)
                Log.i("Analytics","header : "+headers)
                Log.i("Analytics","Build Config : "+buildConfig)
                Log.i("Analytics","Created File Name : "+fileName)

                baseUrl?.let {
                    uploadFileFilter(it, headers.toString(),buildConfig)
                }

                // Delete uploaded content
                 repository.deleteAllAnalytics()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // While extracting file from local Directory filtering based on userid
    private suspend fun uploadFileFilter(baseUrl: String, headers: String, buildConfig: String?) {
        val filteredList = listFilesInDirectory()
        Log.i("Analytics","Total Logs files count : "+filteredList.size)
        filteredList.forEach {
            if (!stopIteration) {
                uploadFileApiCall(baseUrl, headers, it,buildConfig)
            }
        }

    }


    private suspend fun uploadFileApiCall(
        baseUrl: String, header: String, file: File, buildConfig: String?
    ) {
        val filePart = prepareFilePart(file)
        val apiService =  when (buildConfig) {
            "debug" -> initializeApiService(baseUrl, header)
            else -> initializeApiService(baseUrl, header)
        }
//            initializeApiService(baseUrl, header)
        try {
            retryApiCall(3,
                apiCall = {
                    if (apiService!=null) {
                        val response = apiService.uploadFile(filePart)
                        if (response.isSuccessful) {
                            deleteUploadedFiles(file.name)
                            true
                        } else {
                            false
                        }
                    } else {
                        true
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


    private fun deleteUploadedFiles(fileName: String) {
        Log.i("Analytics","Need to Delete File Name : "+fileName)
        val file = File(applicationContext.filesDir, fileName)
        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Log.d("Analytics", "$fileName deleted successfully")
            } else {
                Log.d("Analytics", "Failed to delete $fileName")
            }
        } else {
            Log.d("Analytics", "$fileName does not exist")
        }
    }

    private fun listFilesInDirectory(): List<File> {
        val directory = applicationContext.filesDir
        return directory.listFiles()
            ?.filter { it.name.contains("_analytics.json") }
            ?: emptyList()
    }
}