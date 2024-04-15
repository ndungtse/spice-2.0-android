package com.medtroniclabs.spice.offlinesync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant.KEY_REQUESTS_ID
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineUtils
import com.medtroniclabs.spice.repo.AssessmentRepository
import com.medtroniclabs.spice.repo.HouseHoldRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PostSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted userParameter: WorkerParameters,
    val houseHoldRepository: HouseHoldRepository,
    val assessmentRepository: AssessmentRepository
) : CoroutineWorker(context, userParameter) {

    override suspend fun doWork(): Result {
        try {
            val requestsId = mutableListOf<String>()

            syncHouseHoldsAndMembers()?.let {
                requestsId.add(it)
            }

            val outputData =
                Data.Builder().putStringArray(KEY_REQUESTS_ID, requestsId.toTypedArray()).build()

            return Result.success(outputData)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }

    private suspend fun syncHouseHoldsAndMembers(): String? {
        val householdIds = mutableListOf<Long>()
        val coveredPatientIds = mutableListOf<String>()
        val houseHoldList = houseHoldRepository.getAllUnSyncedHouseHolds()
        houseHoldList.forEach { householdEntity ->
            householdIds.add(householdEntity.referenceId!!.toLong())
            val memberList =
                houseHoldRepository.getAllUnSyncedMembers(householdEntity.referenceId!!.toLong())

            //Assessment
            memberList.forEach { hhm ->
                hhm.motherPatientId?.let { hhm.isChild = true }
                coveredPatientIds.add(hhm.patientId)
                hhm.assessments = assessmentRepository.getUnSyncedAssessmentByPatientId(hhm.patientId)
            }

            householdEntity.householdMembers.addAll(memberList)
        }

        val otherHouseholdMembers = houseHoldRepository.getOtherHouseholdMembers(householdIds)
        //Assessment
        otherHouseholdMembers.forEach { hhm ->
            hhm.motherPatientId?.let { hhm.isChild = true }
            coveredPatientIds.add(hhm.patientId)
            hhm.assessments = assessmentRepository.getUnSyncedAssessmentByPatientId(hhm.patientId)
        }

        val otherAssessments = assessmentRepository.getOtherUnSyncedAssessments(coveredPatientIds)

        val request = OfflineUtils.getRequestObject()
        request[OfflineConstant.HOUSE_HOLDS] = houseHoldList
        request[OfflineConstant.HOUSE_HOLD_MEMBERS] = otherHouseholdMembers
        request[OfflineConstant.ASSESSMENTS] = otherAssessments

        try {
            val apiResponse = houseHoldRepository.postOfflineHouseHolds(request)
            if (apiResponse.isSuccessful) {
                return request[OfflineConstant.REQUEST_ID] as String
            }
        } catch (e: Exception) {
            return null
        }

        return null
    }
}