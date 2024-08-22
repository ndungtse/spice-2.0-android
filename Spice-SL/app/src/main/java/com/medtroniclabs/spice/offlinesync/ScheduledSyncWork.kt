package com.medtroniclabs.spice.offlinesync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.medtroniclabs.spice.appextensions.hideNotification
import com.medtroniclabs.spice.appextensions.showNotification
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.repo.OfflineSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class ScheduledSyncWork @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted userParameter: WorkerParameters,
    val roomHelper: RoomHelper,
    val offlineSyncRepository: OfflineSyncRepository
) : CoroutineWorker(context, userParameter) {

    private val syncDelay = 40 * 1000L // 40 Sec

    override suspend fun doWork(): Result {
        val context = applicationContext
        context.showNotification()

        //1. Update status for old request Id
        if (!getSyncStatus()) {
            context.hideNotification()
            return Result.failure()
        }

        //2. Post Local changes and Get Status
        if (!postLocalChanges()) {
            context.hideNotification()
            return Result.failure()
        }

        //3. Fetch Sync data with last synced at
        context.hideNotification()
        return if (fetchSyncedData())
            Result.success()
        else
            Result.failure()

    }

    private suspend fun getSyncStatus(): Boolean {
        val requestIds =
            SecuredPreference.getStringArray(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
        if (requestIds.isNullOrEmpty()) {
            return true
        }

        val uuid = requestIds[0]

        repeat(4) {
            if (offlineSyncRepository.getSyncStatusForOffline(uuid)) {
                SecuredPreference.remove(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
                return true
            }
            delay(syncDelay)
        }

        return false
    }

    private suspend fun postLocalChanges(): Boolean {
        val requestIds = offlineSyncRepository.postOfflineUnSyncedChanges() ?: return false

        //Save request id in Preference
        if (requestIds.isNotEmpty()) {
            SecuredPreference.saveStringArray(
                SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name,
                requestIds.toTypedArray()
            )

            // Get Status for new request id
            delay(syncDelay)
            if (!getSyncStatus()) {
                return false
            }

            /*Upload images here*/
            offlineSyncRepository.uploadAllSignatures()
        }

        return true
    }

    private suspend fun fetchSyncedData(): Boolean {
        SecuredPreference.remove(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
        val villageIds = roomHelper.getAllVillageIds()
        val lastSyncedAt = SecuredPreference.getString(SecuredPreference.EnvironmentKey.SERVER_LAST_SYNCED.name)
        return offlineSyncRepository.fetchSyncedData(villageIds, lastSyncedAt)
    }
}