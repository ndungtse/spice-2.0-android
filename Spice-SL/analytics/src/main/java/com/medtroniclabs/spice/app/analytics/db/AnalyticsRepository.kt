package com.medtroniclabs.spice.app.analytics.db

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.medtroniclabs.spice.app.analytics.model.AnalyticsData
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils.getAnalyticsFileName
import com.medtroniclabs.spice.app.analytics.utils.UserDetail
import java.io.FileOutputStream

class AnalyticsRepository(ctx: Context) {

    private val dao: AnalyticsDao
    private val context: Context

    init {
        context = ctx
        dao = AnalyticsDatabase.getInstance(ctx).analyticsDao()
    }

    suspend fun logEvent(
        eventType: String,
        parameter: Map<String, Any>,
        referenceId: String? = null
    ): Long {
        return dao.insertAnalytics(
            Analytics(
                userId = UserDetail.userId,
                eventType = eventType,
                parameter = CommonUtils.mapToString(parameter),
                referenceId = referenceId
            )
        )
    }

    suspend fun getParameterByRefIdAndEvent(
        eventType: String,
        referenceId: String
    ): Map<String, Any> {
        return dao.getParameterByRefIdAndEvent(referenceId, eventType)?.let {
            CommonUtils.stringToMap(it)
        } ?: mapOf()
    }

    private suspend fun getAllAnalyticsData(): AnalyticsData {
        val list = dao.getAllAnalytics()
        val userAnalytics = mutableMapOf<String, MutableMap<String, MutableList<JsonElement>>>()

        list.forEach { analytics ->
            // Group by userId
            if (!userAnalytics.containsKey(analytics.userId)) {
                userAnalytics[analytics.userId] = mutableMapOf()
            }
            val analyticsMap = userAnalytics[analytics.userId]

            // Group by event name
            if (!analyticsMap!!.containsKey(analytics.eventType)) {
                analyticsMap[analytics.eventType] = mutableListOf()
            }
            val eventList = analyticsMap[analytics.eventType]

            eventList?.add(JsonParser.parseString(analytics.parameter))

        }

        return AnalyticsData(userAnalytics = userAnalytics)
    }

    suspend fun generateAnalyticsReport() {
        val analyticsData = getAllAnalyticsData()

        try {
            // Create a FileOutputStream
            val outputStream: FileOutputStream = context.openFileOutput(getAnalyticsFileName(), Context.MODE_PRIVATE)

            // Write the JSON data to the FileOutputStream
            outputStream.write(Gson().toJson(analyticsData).toByteArray())

            // Close the FileOutputStream
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}