package com.medtroniclabs.spice

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
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
import com.medtroniclabs.spice.common.SPICE


@HiltAndroidApp
class SpiceBaseApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository


    private var activityCount = 0


    override fun onCreate() {
        super.onCreate()
        initTimber()
        initPreference()
        saveApplicationType()
        getUserJourneyAnalytics()
        handleAppForeground()
    }

    private fun handleAppForeground() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                activityCount++
                if (activityCount == 1) {
                    val backgroundTime = SecuredPreference.getLong(SecuredPreference.EnvironmentKey.BACKGROUNDTIMESTAMP.name, 0L)
                    val currentTime = System.currentTimeMillis()
                    if (backgroundTime != 0L) {
                        val diffInMinutes = (currentTime - backgroundTime) / (1000 * 60)
                        if (diffInMinutes >= 2) {
                            SecuredPreference.putLong(SecuredPreference.EnvironmentKey.BACKGROUNDTIMESTAMP.name,0L)
                            getUserJourneyAnalytics()
                        }
                    }
                }
            }

            override fun onActivityStopped(activity: Activity) {
                activityCount--
                if (activityCount == 0) {
                    // App has gone to background
                    Log.d("SpiceApp", "🌙 App in BACKGROUND")
                    SecuredPreference.putLong(SecuredPreference.EnvironmentKey.BACKGROUNDTIMESTAMP.name,System.currentTimeMillis())
                    // End session tracking, etc.
                }
            }

            // Other lifecycle methods — no-op
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun saveApplicationType() {
        SecuredPreference.putString(SecuredPreference.EnvironmentKey.APPLICATION.name, getApplicationName())
    }

    private fun getApplicationName() : String {
        val packageName = applicationContext.packageName
        return when {
            packageName.contains(".sl") -> SPICE.SIERRA_LEONE.name
            else -> SPICE.AFRICA.name
        }
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
                        userId= it.value[0].userID,
                        eventName = AnalyticsDefinedParams.SessionTracking,
                        referenceId = it.key,
                        userJourney = it.value, analyticsRepository = analyticsRepository
                    )
            }
           analyticsRepository.deleteAllUserJourneys(UserDetail.referenceId)
        }
    }

    private fun groupingBySessionId(list: List<UserJourneyAnalytics>): Pair<String, MutableMap<String, List<ScreenDetails>>>? {
        return when {
            list.isNotEmpty() -> {
                Pair(list[0].userId, list.groupBy(UserJourneyAnalytics::sessionId)
                    .mapValues { (_, analyticsList) ->
                        analyticsList.map { ScreenDetails(it.userJourney, it.startTime ?: "",it.userId,it.userRole) }
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

