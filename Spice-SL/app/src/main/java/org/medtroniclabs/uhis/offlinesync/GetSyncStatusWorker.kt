package org.medtroniclabs.uhis.offlinesync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import org.medtroniclabs.uhis.appextensions.hideNotification
import org.medtroniclabs.uhis.appextensions.showNotification
import org.medtroniclabs.uhis.appextensions.startBackgroundOfflineSync
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.common.StringConverter
import org.medtroniclabs.uhis.data.offlinesync.utils.OfflineConstant.KEY_REQUESTS_ID
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.ncd.screening.repo.ScreeningRepository
import org.medtroniclabs.uhis.ncd.screening.utils.SignatureRequestBody
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import org.medtroniclabs.uhis.repo.AssessmentRepository
import org.medtroniclabs.uhis.repo.OfflineSyncRepository
import org.medtroniclabs.uhis.repo.RxBuddyRepository
import javax.inject.Inject

@HiltWorker
class GetSyncStatusWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted userParameter: WorkerParameters,
    val roomHelper: RoomHelper,
    val offlineSyncRepository: OfflineSyncRepository,
    val rxBuddyRepository: RxBuddyRepository,
    private val screeningRepository: ScreeningRepository,
    private val assessmentRepository: AssessmentRepository,
) : CoroutineWorker(context, userParameter) {
    @Inject
    lateinit var connectivityManager: ConnectivityManager

    private val retryCount = 15 // 1 (1*4 - try) initial Call + 3 (3*4 - try) retry
    private val timeDelayForPolling = (45 * 1000L) // 45 seconds once
    private val syncDelay = 10 * 1000L // 40 Sec
    private val ncdRetry = 3

    override suspend fun doWork(): Result {
        val context = applicationContext
        if (CommonUtils.isCommunity()) {
            val requestIds = inputData.getStringArray(KEY_REQUESTS_ID)

            repeat(retryCount) {
                if (!connectivityManager.isNetworkAvailable()) {
                    return Result.failure(getFailure())
                }

                var isAllEntitiesSynced = true
                if (!requestIds.isNullOrEmpty()) {
                    isAllEntitiesSynced =
                        offlineSyncRepository.getSyncStatusForOffline(requestIds[0])
                }

                if (isAllEntitiesSynced) {
                    SecuredPreference.remove(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
                    /*
                     * Rx Buddy Sync for new household
                     * */
                    /*val unSyncedRxBuddyRegister = rxBuddyRepository.getUnSyncedRxBuddyRegisterCount()
                    if (unSyncedRxBuddyRegister > 0) {
                        val rxBuddyRequestIds = offlineSyncRepository.postOfflineUnSyncedChangesWithMutex(OfflineConstant.SYNC_MODE_AUTOMATIC)
                        if (!rxBuddyRequestIds.isNullOrEmpty()) {
                            delay(syncDelay)
                            offlineSyncRepository.getSyncStatusForOffline(rxBuddyRequestIds[0])

                        }
                    }*/

                    // Upload images here
                    offlineSyncRepository.uploadAllSignatures()

                    val villageIds = roomHelper.getAllVillageIds()
                    val lastSyncedAt =
                        SecuredPreference.getString(SecuredPreference.EnvironmentKey.SERVER_LAST_SYNCED.name)
                    return if (offlineSyncRepository.fetchSyncedData(villageIds, lastSyncedAt)) {
                        Result.success()
                    } else {
                        Result.failure()
                    }
                } else {
                    delay(timeDelayForPolling)
                }
            }
        } else {
            repeat(ncdRetry) {
                var screeningSuccess = true
                var assessmentSuccess = true

                try {
                    if (!connectivityManager.isNetworkAvailable()) {
                        return Result.failure(getFailure())
                    }

                    val screeningUploadList = screeningRepository.getAllScreeningRecords(false)
                    if (screeningUploadList?.isNotEmpty() == true) {
                        context.showNotification()
                        screeningUploadList.forEach { data ->
                            val dataId = data.id
                            // Make API call
                            val requestBody = SignatureRequestBody.signatureRequestBody(
                                applicationContext,
                                Triple(data.generalDetails, data.screeningDetails, data.userId),
                                data.signature,
                                true,
                            )
                            val response = screeningRepository.createScreeningLog(requestBody)
                            if (response.isSuccessful) {
                                // If the API call is successful, update the record
                                screeningRepository.updateScreeningRecordById(dataId, true)
                            } else {
                                // Set screening success flag to false if any call fails
                                screeningSuccess = false
                            }
                        }
                    }

                    // Delete uploaded screening records after all uploads are successful
                    if (screeningSuccess) {
                        screeningRepository.deleteUploadedScreeningRecords(DateUtils.getTodayDateInMilliseconds())
                    }

                    // Proceed with assessment regardless of screening result
                    val assessmentOfflineList = assessmentRepository.getAssessmentOfflineList(false)
                    if (assessmentOfflineList.isNotEmpty()) {
                        assessmentOfflineList.forEach {
                            val dataId = it.id
                            val reqMap = StringConverter.convertStringToMap(it.assessmentDetails)
                            val response = assessmentRepository.createAssessmentNCD(
                                StringConverter.getJsonObject(Gson().toJson(reqMap)),
                            )

                            if (response.isSuccessful) {
                                // If API is successful, update the assessment record
                                assessmentRepository.updateAssessmentUploadStatus(dataId, true)
                            } else {
                                // Set assessment success flag to false if any call fails
                                assessmentSuccess = false
                            }
                        }
                    }

                    // Delete uploaded assessment records if successful
                    if (assessmentSuccess) {
                        assessmentRepository.deleteAssessmentList(true)
                    }

                    // Return success if both screening and assessment were successful
                    if (screeningSuccess && assessmentSuccess) {
                        context.hideNotification()
                        if (CommonUtils.isChp()) {
                            context.startBackgroundOfflineSync()
                        }
                        return Result.success()
                    }
                } catch (e: Exception) {
                    context.hideNotification()
                    e.printStackTrace()
                    // Continue to next attempt
                }
            }
        }
        context.hideNotification()
        return Result.failure()
    }

    private fun getFailure(): Data =
        Data
            .Builder()
            .putString("failureReason", "Network Not available")
            .build()
}
