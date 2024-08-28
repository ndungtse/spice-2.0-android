package com.medtroniclabs.spice.offlinesync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant.KEY_REQUESTS_ID
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.ncd.screening.repo.ScreeningRepository
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.OfflineSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltWorker
class GetSyncStatusWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted userParameter: WorkerParameters,
    val roomHelper: RoomHelper,
    val offlineSyncRepository: OfflineSyncRepository,
    private val screeningRepository: ScreeningRepository
) : CoroutineWorker(context, userParameter) {

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    private val retryCount = 15 // 1 (1*4 - try) initial Call + 3 (3*4 - try) retry
    private val timeDelayForPolling = (45 * 1000L) // 45 seconds once
    private val ncdRetry = 3

    override suspend fun doWork(): Result {
        if (CommonUtils.isNonNcdWorkflow()) {
            val requestIds = inputData.getStringArray(KEY_REQUESTS_ID)

            repeat(retryCount) {
                if (!connectivityManager.isNetworkAvailable()) {
                    val errorData = Data.Builder()
                        .putString("failureReason", "Network Not available")
                        .build()
                    return Result.failure(errorData)
                }

                var isAllEntitiesSynced = true
                if (!requestIds.isNullOrEmpty()) {
                    isAllEntitiesSynced =
                        offlineSyncRepository.getSyncStatusForOffline(requestIds[0])
                }

                if (isAllEntitiesSynced) {
                    /*Upload images here*/
                    offlineSyncRepository.uploadAllSignatures()

                    SecuredPreference.remove(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
                    val villageIds = roomHelper.getAllVillageIds()
                    val lastSyncedAt =
                        SecuredPreference.getString(SecuredPreference.EnvironmentKey.SERVER_LAST_SYNCED.name)
                    return if (offlineSyncRepository.fetchSyncedData(villageIds, lastSyncedAt))
                        Result.success()
                    else
                        Result.failure()
                } else
                    delay(timeDelayForPolling)
            }
        } else {
            repeat(ncdRetry) {
                try {
                    if (!connectivityManager.isNetworkAvailable()) {
                        // If the network is not available, no need to continue
                        return Result.failure()
                    }

                    val screeningUploadList = screeningRepository.getAllScreeningRecords(false)
                    screeningUploadList?.forEach { data ->
                        val dataId = data.id
                        // Make API call
                        val response = screeningRepository.createScreeningLog(
                            StringConverter.getJsonObject(
                                Gson().toJson(
                                    CommonUtils.parseRequest(
                                        data.generalDetails,
                                        data.screeningDetails,
                                        data.userId
                                    )
                                )
                            )
                        )
                        // Check if the API call was successful
                        if (response.isSuccessful) {
                            // If the API call is successful, update the record
                            screeningRepository.updateScreeningRecordById(dataId, true)
                        } else {
                            // If one of the API calls fails, retry
                            return@repeat
                        }
                    }

                    // Delete uploaded records only after all uploads are successful
                    screeningRepository.deleteUploadedScreeningRecords(DateUtils.getTodayDateInMilliseconds())

                    // If everything is successful, return success
                    return Result.success()
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Continue to next attempt
                }
            }
        }
        return Result.failure()
    }
}