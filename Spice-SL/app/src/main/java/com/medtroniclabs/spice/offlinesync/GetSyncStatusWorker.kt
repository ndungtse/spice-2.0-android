package com.medtroniclabs.spice.offlinesync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant.KEY_REQUESTS_ID
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.repo.OfflineSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class GetSyncStatusWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted userParameter: WorkerParameters,
    val roomHelper: RoomHelper,
    val offlineSyncRepository: OfflineSyncRepository
) : CoroutineWorker(context, userParameter) {

    private val retryCount = 4 // 1 initial Call + 3 retry
    private val timeDelayForPolling = (1 * 30 * 1000L) // 3 mintues

    override suspend fun doWork(): Result {
        val requestIds = inputData.getStringArray(KEY_REQUESTS_ID)

        repeat(retryCount) {
            Log.e("TEST","Retry Count : $it")
            var isAllEntitiesSynced = true
            if (!requestIds.isNullOrEmpty()) {
                isAllEntitiesSynced = getSyncStatusForHouseHolds(requestIds[0])
            }

            if (isAllEntitiesSynced) {
                SecuredPreference.remove(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
                offlineSyncRepository.fetchSyncedData()
                return Result.success()
            } else
                delay(timeDelayForPolling)
        }

        return Result.failure()
    }

    private suspend fun getSyncStatusForHouseHolds(id: String): Boolean {

        val req = RequestGetSyncStatus(requestId = id)

        try {
            // Get Sync Status
            val response = offlineSyncRepository.getSyncStatus(req)
            if (response.isSuccessful) {
                var isAllEntitiesSynced = true
                response.body()?.entityList?.forEach { entity ->
                   when(entity.status) {
                       OfflineSyncStatus.Success.name -> {
                           if (entity.type != null && entity.referenceId != null && entity.fhirId != null) {
                               offlineSyncRepository.updateFhirId(
                                   entity.type,
                                   entity.referenceId,
                                   entity.fhirId,
                                   OfflineSyncStatus.Success.name
                               )
                           }
                       }

                       OfflineSyncStatus.Failed.name -> {
                           if (entity.type != null && entity.referenceId != null) {
                               offlineSyncRepository.updateFhirId(
                                   entity.type,
                                   entity.referenceId,
                                   null,
                                   OfflineSyncStatus.Failed.name
                               )
                           }
                       }

                       OfflineSyncStatus.InProgress.name -> {
                           isAllEntitiesSynced = false
                       }
                   }
                }

                return isAllEntitiesSynced
            } else {
                return false
            }
        } catch (e: Exception) {
            Log.e("Test","Exception : "+e.message)
            return false
        }
    }
}