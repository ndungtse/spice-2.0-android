package com.medtroniclabs.spice.offlinesync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant.KEY_REQUESTS_ID
import com.medtroniclabs.spice.db.local.RoomHelper
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
    val offlineSyncRepository: OfflineSyncRepository
) : CoroutineWorker(context, userParameter) {

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    private val retryCount = 15 // 1 (1*4 - try) initial Call + 3 (3*4 - try) retry
    private val timeDelayForPolling = (45 * 1000L) // 45 seconds once

    override suspend fun doWork(): Result {

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
                isAllEntitiesSynced = offlineSyncRepository.getSyncStatusForOffline(requestIds[0])
            }

            if (isAllEntitiesSynced) {
                /*Upload images here*/
                offlineSyncRepository.uploadAllSignatures()

                SecuredPreference.remove(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
                val villageIds = roomHelper.getAllVillageIds()
                val lastSyncedAt = SecuredPreference.getString(SecuredPreference.EnvironmentKey.SERVER_LAST_SYNCED.name)
                return if (offlineSyncRepository.fetchSyncedData(villageIds, lastSyncedAt))
                    Result.success()
                else
                    Result.failure()
            } else
                delay(timeDelayForPolling)
        }

        return Result.failure()
    }
}