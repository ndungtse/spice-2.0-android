package com.medtroniclabs.spice.app.analytics.db

import android.content.Context
import com.medtroniclabs.spice.app.analytics.model.Analytics
import com.medtroniclabs.spice.app.analytics.model.UserJourneyAnalytics
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils
import com.medtroniclabs.spice.app.analytics.model.UserDetail

class AnalyticsRepository(private val ctx: Context) {

    private val dao: AnalyticsDao by lazy {
        AnalyticsDatabase.getInstance(ctx).analyticsDao()
    }


    suspend fun logEvent(
        eventType: String, parameter: Map<String, Any?>,lastSyncDate:String?): Long = dao.insertAnalytics(
        Analytics(
            userId = UserDetail.userId,
            role = UserDetail.role,
            eventType = eventType,
            parameter = CommonUtils.mapToString(parameter),
            lastSyncDate = lastSyncDate
        )
    )

    suspend fun logEvent(
        userId: String,
        userRole: String,
        eventType: String,
        parameter: Map<String, Any?>,
        lastSyncDate:String?): Long = dao.insertAnalytics(
        Analytics(
            userId = userId,
            role = userRole,
            eventType = eventType,
            parameter = CommonUtils.mapToString(parameter),
            lastSyncDate = lastSyncDate
        )
    )

    suspend fun getAllAnalytics(): List<Analytics> = dao.getAllAnalytics()

    suspend fun insertUserJourney(userJourney: String) = dao.insertUserJourneyAnalytics(
        UserJourneyAnalytics(
            userId= UserDetail.userId,
            sessionId = UserDetail.referenceId,
            userJourney = userJourney,
            startTime = UserDetail.startDateTime,
            userRole = UserDetail.role
        )
    )
    suspend fun getUserJourneyAnalytics(): List<UserJourneyAnalytics> = dao.getUserJourney()

    suspend fun deleteAllUserJourneys(referenceId: String) =dao.deleteAllUserJourneys(referenceId)

}