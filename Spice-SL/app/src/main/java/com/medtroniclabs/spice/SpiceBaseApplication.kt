package com.medtroniclabs.spice

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.medtroniclabs.spice.app.analytics.db.AnalyticsRepository
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils
import com.medtroniclabs.spice.appextensions.isDebug
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.log.CrashReportingTree
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import com.medtroniclabs.spice.app.analytics.model.ScreenDetails
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.model.UserJourneyAnalytics


@HiltAndroidApp
class SpiceBaseApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository


    override fun onCreate() {
        super.onCreate()
        initTimber()
        initPreference()
        getUserJourneyAnalytics()
        saveApplicationType()
    }

    private fun saveApplicationType() {
        SecuredPreference.putString(SecuredPreference.EnvironmentKey.APPLICATION.name, com.medtroniclabs.spice.common.CommonUtils.requestFrom())
    }


    /**
     * method to initialize preference
     */
    private fun initPreference() {
        SecuredPreference
            .build(packageName, applicationContext)
    }

    /**
     * method to print debug and release logs
     */
    private fun initTimber() {
        isDebug { debug ->
            if (debug)
                Timber.plant(Timber.DebugTree())
            else
                Timber.plant(CrashReportingTree())
        }
    }

    private fun getUserJourneyAnalytics() {
        SecuredPreference.getString(AnalyticsDefinedParams.SessionId)?.let {
            getUserJourneyList()
        } ?: run {
            UserDetail.referenceId = UUID.randomUUID().toString()
//            UserDetail.role = SecuredPreference.getRole().toString()
            SecuredPreference.putString(AnalyticsDefinedParams.SessionId, UserDetail.referenceId)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun getUserJourneyList() {
        GlobalScope.launch {
            val list = analyticsRepository.getUserJourneyAnalytics()
            val userAnalytics = groupingBySessionId(list)
            UserDetail.referenceId = UUID.randomUUID().toString()
            UserDetail.role = SecuredPreference.getRole()?:""
            SecuredPreference.putString(
                AnalyticsDefinedParams.SessionId,
                UserDetail.referenceId
            )
            userAnalytics?.second?.forEach {
                CommonUtils.setUserJourneyData(
                        userId=userAnalytics.first,
                        eventName = AnalyticsDefinedParams.SessionTracking,
                        referenceId = it.key,
                        userJourney = it.value, analyticsRepository = analyticsRepository
                    )
            }
            analyticsRepository.deleteAllUserJourneys()
        }
    }

    private fun groupingBySessionId(list: List<UserJourneyAnalytics>): Pair<String, MutableMap<String, List<ScreenDetails>>>? {
        return when {
            list.isNotEmpty() -> {
                Pair(list[0].userId, list.groupBy(UserJourneyAnalytics::sessionId)
                    .mapValues { (_, analyticsList) ->
                        analyticsList.map { ScreenDetails(it.userJourney, it.startTime ?: "") }
                    }.toMutableMap()
                )
            }

            else -> {
                return null
            }
        }
    }


    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(hiltWorkerFactory).build()


}

