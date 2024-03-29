package com.medtroniclabs.spice.offlinesync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.medtroniclabs.spice.offlinesync.model.RequestGetSyncStatus
import com.medtroniclabs.spice.offlinesync.utils.OfflineConstant.KEY_REQUESTS_ID
import com.medtroniclabs.spice.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.repo.HouseHoldRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class GetSyncStatusWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted userParameter: WorkerParameters,
    val houseHoldRepository: HouseHoldRepository
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

            if (isAllEntitiesSynced)
                return Result.success()
            else
                delay(timeDelayForPolling)
        }

        return Result.failure()
    }

    private suspend fun getSyncStatusForHouseHolds(id: String): Boolean {

        val req = RequestGetSyncStatus(requestId = id)

        try {
            val response = houseHoldRepository.getSyncStatus(req)
            if (response.isSuccessful) {
                var isAllEntitiesSynced = true
                response.body()?.entityList?.forEach { entity ->
                    if (entity.status == OfflineSyncStatus.Success.name && entity.type != null && entity.referenceId != null && entity.fhirId != null)
                        houseHoldRepository.updateFhirId(
                            entity.type,
                            entity.referenceId,
                            entity.fhirId
                        )
                    else
                        isAllEntitiesSynced = false
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