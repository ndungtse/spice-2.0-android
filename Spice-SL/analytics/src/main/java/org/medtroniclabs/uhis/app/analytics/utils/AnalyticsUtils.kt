package org.medtroniclabs.uhis.app.analytics.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.medtroniclabs.uhis.app.analytics.db.AnalyticsRepository
import org.medtroniclabs.uhis.app.analytics.model.ScreenDetails
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object AnalyticsUtils {
    fun mapToString(map: Map<String, Any?>): String = Gson().toJson(map)

    fun stringToMap(input: String): Map<String, Any> {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return Gson().fromJson(input, mapType)
    }

    fun getCurrentDateTimeInUTC(): String {
        val currentTime = System.currentTimeMillis()
        val dateFormat =
            SimpleDateFormat(AnalyticsDefinedParams.YYYYMMDDHHMMSS, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(currentTime)
    }

    fun getAnalyticsFileName(ids: String): String {
        val strBuilder = StringBuilder()
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        strBuilder.append(dateFormat.format(currentTime))
        strBuilder.append("_")
        strBuilder.append(ids)
        strBuilder.append("_")
        strBuilder.append("analytics.json")
        return strBuilder.toString()
    }

    fun createEventParameter(
        startTime: String? = null,
        eventType: String? = null,
        exitReason: String? = null,
        isCompleted: String? = null,
        apiId: Int? = null,
        referenceId: String? = null,
        userJourney: List<ScreenDetails>? = null,
        endTime: String? = getCurrentDateTimeInLocalTime(),
    ): Map<String, Any?> =
        mutableMapOf(
            AnalyticsDefinedParams.StartTime to startTime,
            AnalyticsDefinedParams.EndTime to endTime,
            AnalyticsDefinedParams.EventType to eventType,
            AnalyticsDefinedParams.IsCompleted to isCompleted,
            AnalyticsDefinedParams.ExitReason to exitReason,
            AnalyticsDefinedParams.ApiId to apiId,
            AnalyticsDefinedParams.ReferenceId to referenceId,
            AnalyticsDefinedParams.UserJourney to userJourney,
        )

    fun createFollowUpEventParameter(
        id: Long,
        patientId: String?,
        callStatus: String,
        patientStatus: String?,
        unSuccessfulReason: String?,
        startTiming: String?,
    ): Map<String, Any?> =
        mutableMapOf(
            AnalyticsDefinedParams.FollowUpId to id,
            AnalyticsDefinedParams.PatientId to patientId,
            AnalyticsDefinedParams.CallStatus to callStatus,
            AnalyticsDefinedParams.PatientStatus to patientStatus,
            AnalyticsDefinedParams.UnSuccessFullReason to unSuccessfulReason,
            AnalyticsDefinedParams.StartTime to startTiming,
            AnalyticsDefinedParams.EndTime to getCurrentDateTimeInLocalTime(),
        )

    fun UserDetail.updateUserIdIfEmpty(userId: String): String =
        this.userId.ifEmpty {
            this.userId = userId
            userId
        }

    fun getCurrentDateTimeInLocalTime(): String {
        val currentTime = System.currentTimeMillis()
        val dateFormat =
            SimpleDateFormat(AnalyticsDefinedParams.YYYYMMDDHHMMSS, Locale.getDefault())
        return dateFormat.format(currentTime)
    }

    fun UserDetail.getAppVersion(versionName: String): String =
        this.appVersion.ifEmpty {
            this.appVersion = versionName
            versionName
        }

    suspend fun setUserJourneyData(
        eventName: String,
        referenceId: String?,
        userJourney: List<ScreenDetails>?,
        analyticsRepository: AnalyticsRepository,
        userId: String,
        lastSyncedAt: String? = null,
    ) {
        val parameter = createEventParameter(
            endTime = null,
            referenceId = referenceId,
            userJourney = userJourney,
        )
        val role: String = if (!userJourney.isNullOrEmpty()) {
            userJourney[0].userRole ?: UserDetail.role
        } else {
            UserDetail.role
        }
        analyticsRepository.logEvent(userId, userRole = role, eventName, parameter, lastSyncedAt)
    }
}
