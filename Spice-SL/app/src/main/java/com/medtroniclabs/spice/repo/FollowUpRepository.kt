package com.medtroniclabs.spice.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.medtroniclabs.spice.appextensions.convertToUtcDateTime
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams.OnTreatment
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.data.offlinesync.model.FollowUpCallStatus
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import com.medtroniclabs.spice.db.entity.FollowUp
import com.medtroniclabs.spice.db.entity.FollowUpCall
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.followup.FollowUpFilter
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.followup.FollowUpDefinedParams
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class FollowUpRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper
) {

    fun getFollowUpListLiveData(filter: FollowUpFilter): LiveData<List<FollowUpPatientModel>> {
        val villageIds = if (filter.selectedVillages.isNullOrEmpty()) {
            filter.villages
        } else {
            filter.selectedVillages!!.map { it.id!! }
        }

        val fromAndToDate = getFromDateAndToDate(filter)

        return roomHelper.getFollowUpPatientListLiveData(
            filter.type,
            filter.search,
            villageIds,
            fromAndToDate.first,
            fromAndToDate.second
        ).map { list -> list.sortedBy { it.updatedAt } }
    }

    private fun getFromDateAndToDate(filter: FollowUpFilter): Pair<String, String> {
        if (filter.selectedDateRange.isNullOrEmpty()) {
            return Pair("", "")
        }

        if (filter.selectedDateRange?.any { it.name == FollowUpDefinedParams.FilterToday } == true) {
            val date = DateUtils.getTodayStringDate()
            return Pair(date, date)
        }

        if (filter.selectedDateRange?.any { it.name == FollowUpDefinedParams.FilterTomorrow } == true) {
            val date = DateUtils.getTomorrowStringDate()
            return Pair(date, date)
        }

        if (filter.selectedDateRange?.any { it.name == FollowUpDefinedParams.FilterCustomize } == true) {
            val fromDate = DateUtils.convertDateFormat(
                filter.fromDate,
                DateUtils.DATE_ddMMyyyy,
                DateUtils.DATE_FORMAT_yyyyMMdd
            )
            val toDate = DateUtils.convertDateFormat(
                filter.toDate,
                DateUtils.DATE_ddMMyyyy,
                DateUtils.DATE_FORMAT_yyyyMMdd
            )
            return Pair(fromDate, toDate)
        }

        return Pair("", "")
    }

    suspend fun getVillageIds(): List<VillageEntity> {
        return roomHelper.getAllVillageEntity()
    }

    suspend fun getUnSyncedFollowUpCount(): Int {
        return roomHelper.getUnSyncedFollowUpCount()
    }

    suspend fun addCallHistory(
        maxSuccessfulCallLimit: Int,
        maxUnSuccessfulCallLimit: Int,
        followUpId: Long,
        callStatus: FollowUpCallStatus,
        patientStatus: String? = null,
        reason: String? = null
    ) {
        val followUp = roomHelper.getFollowUpById(followUpId)
        followUp.syncStatus = OfflineSyncStatus.NotSynced
        followUp.attempts = followUp.attempts + 1

        val callDetail = FollowUpCall(
            followUpId = followUpId,
            callDate = System.currentTimeMillis().convertToUtcDateTime(),
            duration = 0,
            attempts =  followUp.attempts,
            status = callStatus,
            patientStatus = patientStatus,
            reason = reason
        )

        var newFollowUp: FollowUp? = null
        if (callDetail.status == FollowUpCallStatus.SUCCESSFUL) {
            newFollowUp = handleSuccessCall(followUpId, followUp, callDetail, maxSuccessfulCallLimit)
        } else {
            handleUnSuccessfulCall(followUp, callDetail, maxUnSuccessfulCallLimit)
        }

        roomHelper.addCallHistory(followUp, callDetail, newFollowUp)
    }

    private suspend fun handleSuccessCall(id: Long,followUp: FollowUp, call: FollowUpCall, maxSuccessfulCallLimit: Int): FollowUp? {
        // Get All other followup with same reason
        call.patientStatus?.let {
            updatePatientStatus(it, id, followUp)
        }

        followUp.successfulAttempts = followUp.successfulAttempts + 1
        if (followUp.successfulAttempts >= maxSuccessfulCallLimit) {
            followUp.isCompleted = true
        }

        if ((followUp.type == FollowUpDefinedParams.FU_TYPE_HH_VISIT || followUp.type == FollowUpDefinedParams.FU_TYPE_MEDICAL_REVIEW)
            && call.patientStatus?.equals(ReferralStatus.Referred.name, true) == true
        ) {
            return getNewFollowUp(
                followUp,
                FollowUpDefinedParams.FU_TYPE_REFERRED,
                ReferralStatus.Referred.name,
                referredSiteId = SecuredPreference.getOrganizationFhirId()
            )
        }

        return null
    }

    private suspend fun updatePatientStatus(status: String, id: Long, followUp: FollowUp) {
        followUp.currentPatientStatus = status
        val currentTime = System.currentTimeMillis()
        followUp.updatedAt = currentTime
        followUp.calledAt = currentTime
        when (status.lowercase()) {
            ReferralStatus.Recovered.name.lowercase() -> {
                followUp.isCompleted = true
                roomHelper.updateOtherDuplicateTickets(id, followUp)
            }

            ReferralStatus.Referred.name.lowercase() -> {
                if (followUp.type == FollowUpDefinedParams.FU_TYPE_REFERRED) {
                    roomHelper.updateOnTreatmentStatus(id, followUp, currentTime)
                } else {
                    roomHelper.updateOtherDuplicateTickets(id, followUp)
                }
            }

            else -> {
                roomHelper.updateOnTreatmentStatus(id, followUp, currentTime)
            }
        }
    }

    private fun handleUnSuccessfulCall(followUp: FollowUp, call: FollowUpCall, maxUnSuccessfulCallLimit: Int) {
        followUp.unsuccessfulAttempts = followUp.unsuccessfulAttempts + 1
        if (followUp.unsuccessfulAttempts >= maxUnSuccessfulCallLimit) {
            followUp.isCompleted = true
        }

        if (call.reason?.equals(FollowUpDefinedParams.WRONG_NUMBER, true) == true) {
            followUp.isCompleted = true
        }
    }

    private fun getNewFollowUp(followUp: FollowUp, type: String, status: String, referredSiteId: String? = null, nextVisitDate: String? = null): FollowUp {
        followUp.isCompleted = true
        return followUp.copy(
            referenceId = 0,
            id = null,
            type = type,
            patientStatus = status,
            currentPatientStatus = null,
            isCompleted = false,
            attempts = 0,
            successfulAttempts = 0,
            unsuccessfulAttempts = 0,
            encounterDate = System.currentTimeMillis().convertToUtcDateTime(),
            nextVisitDate = nextVisitDate,
            referredSiteId = referredSiteId
        )
    }

}