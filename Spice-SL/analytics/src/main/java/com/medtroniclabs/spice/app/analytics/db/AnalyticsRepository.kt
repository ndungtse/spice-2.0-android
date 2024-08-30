package com.medtroniclabs.spice.app.analytics.db

import android.content.Context
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils
import com.medtroniclabs.spice.app.analytics.utils.UserDetail

class AnalyticsRepository(private val ctx: Context) {

    private val dao: AnalyticsDao by lazy {
        AnalyticsDatabase.getInstance(ctx).analyticsDao()
    }


    suspend fun logEvent(
        eventType: String, parameter: Map<String, Any?>): Long = dao.insertAnalytics(
        Analytics(
            userId = UserDetail.userId,
            eventType = eventType,
            parameter = CommonUtils.mapToString(parameter),
        )
    )


    suspend fun getParameterByRefIdAndEvent(
        eventType: String, referenceId: String
    ): Map<String, Any> = dao.getParameterByRefIdAndEvent(referenceId, eventType)?.let {
        CommonUtils.stringToMap(it)
    } ?: mapOf()


    suspend fun getAllAnalytics(): List<Analytics> = dao.getAllAnalytics()

    suspend fun insertUserJourney(userJourney: String) = dao.insertUserJourneyAnalytics(
        UserJourneyAnalytics(
            userId=UserDetail.userId,
            sessionId = UserDetail.referenceId,
            userJourney = userJourney
        )
    )
    suspend fun getUserJourneyAnalytics(): List<UserJourneyAnalytics> = dao.getUserJourney()

    suspend fun deleteAllUserJourneys()=dao.deleteAllUserJourneys()

}