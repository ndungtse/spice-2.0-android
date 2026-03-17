package org.medtroniclabs.uhis.app.analytics.db

import android.content.Context
import org.medtroniclabs.uhis.app.analytics.model.Analytics
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.model.UserJourneyAnalytics
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsUtils

class AnalyticsRepository(private val ctx: Context) {
    private val dao: AnalyticsDao by lazy {
        AnalyticsDatabase.getInstance(ctx).analyticsDao()
    }

    suspend fun logEvent(
        eventType: String,
        parameter: Map<String, Any?>,
        lastSyncDate: String?,
    ): Long =
        dao.insertAnalytics(
            Analytics(
                userId = UserDetail.userId,
                role = UserDetail.role,
                eventType = eventType,
                parameter = AnalyticsUtils.mapToString(parameter),
                lastSyncDate = lastSyncDate,
            ),
        )

    suspend fun logEvent(
        userId: String,
        userRole: String,
        eventType: String,
        parameter: Map<String, Any?>,
        lastSyncDate: String?,
    ): Long =
        dao.insertAnalytics(
            Analytics(
                userId = userId,
                role = userRole,
                eventType = eventType,
                parameter = AnalyticsUtils.mapToString(parameter),
                lastSyncDate = lastSyncDate,
            ),
        )

    suspend fun getAllAnalytics(): List<Analytics> = dao.getAllAnalytics()

    suspend fun insertUserJourney(userJourney: String) =
        dao.insertUserJourneyAnalytics(
            UserJourneyAnalytics(
                userId = UserDetail.userId,
                sessionId = UserDetail.referenceId,
                userJourney = userJourney,
                startTime = UserDetail.startDateTime,
                userRole = UserDetail.role,
            ),
        )

    suspend fun getUserJourneyAnalytics(): List<UserJourneyAnalytics> = dao.getUserJourney()

    suspend fun deleteAllUserJourneys(referenceId: String) = dao.deleteAllUserJourneys(referenceId)

    suspend fun deleteAllAnalytics() = dao.deleteAllAnalytics()

    suspend fun deleteAllUserJourneys() = dao.deleteAllUserJourneys()
}
