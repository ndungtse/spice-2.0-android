package com.medtroniclabs.spice.offlinesync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineUtils
import com.medtroniclabs.spice.repo.OfflineSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PostSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted userParameter: WorkerParameters,
    private val offlineSyncRepo: OfflineSyncRepository
) : CoroutineWorker(context, userParameter) {

    override suspend fun doWork(): Result {
        try {
            val requestsId = mutableListOf<String>()

            syncHouseHoldsAndMembers()?.let {
                requestsId.add(it)
            }

            SecuredPreference.saveStringArray(
                SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name,
                requestsId.toTypedArray()
            )

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }

    private suspend fun syncHouseHoldsAndMembers(): String? {
        val houseHoldList = offlineSyncRepo.getAllUnSyncedHouseHolds()
        houseHoldList.forEach { householdEntity ->
            val memberList =
                offlineSyncRepo.getAllUnSyncedMembers(householdEntity.referenceId!!.toLong())

            //Assessment
            memberList.forEach { hhm ->
                hhm.motherPatientId?.let { hhm.isChild = true }
                hhm.assessments = offlineSyncRepo.getUnSyncedAssessmentByPatientId(hhm.patientId)
            }

            householdEntity.householdMembers.addAll(memberList)
        }

        val otherHouseholdMembers = offlineSyncRepo.getOtherHouseholdMembers()
        //Assessment
        otherHouseholdMembers.forEach { hhm ->
            hhm.motherPatientId?.let { hhm.isChild = true }
            hhm.assessments = offlineSyncRepo.getUnSyncedAssessmentByPatientId(hhm.patientId)
        }

        val otherAssessments = offlineSyncRepo.getOtherUnSyncedAssessments()

        val request = OfflineUtils.getRequestObject()
        request[OfflineConstant.HOUSE_HOLDS] = houseHoldList
        request[OfflineConstant.HOUSE_HOLD_MEMBERS] = otherHouseholdMembers
        request[OfflineConstant.ASSESSMENTS] = otherAssessments

        try {
            val apiResponse = offlineSyncRepo.postOfflineHouseHolds(request)
            if (apiResponse.isSuccessful) {
                return request[OfflineConstant.REQUEST_ID] as String
            }
        } catch (e: Exception) {
            return null
        }

        return null
    }
}