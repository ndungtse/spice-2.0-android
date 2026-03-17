package org.medtroniclabs.uhis.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import org.medtroniclabs.uhis.appextensions.convertToUtcDateTime
import org.medtroniclabs.uhis.common.DateUtils.DATE_FORMAT_yyyyMMdd
import org.medtroniclabs.uhis.common.DateUtils.DATE_ddMMyyyy
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.FollowUpPatientModel
import org.medtroniclabs.uhis.data.offlinesync.model.FollowUpCallStatus
import org.medtroniclabs.uhis.data.offlinesync.utils.OfflineSyncStatus
import org.medtroniclabs.uhis.db.entity.FollowUp
import org.medtroniclabs.uhis.db.entity.FollowUpCall
import org.medtroniclabs.uhis.db.entity.VillageEntity
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.model.followup.FollowUpFilter
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.ui.assessment.referrallogic.utils.ReferralStatus
import org.medtroniclabs.uhis.ui.followup.FollowUpDefinedParams
import org.medtroniclabs.uhis.ui.followup.FollowUpDefinedParams.INFORMED
import org.medtroniclabs.uhis.ui.followup.FollowUpDefinedParams.NOT_INFORMED
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class FollowUpRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper,
) {
    fun getFollowUpListLiveData(
        filter: FollowUpFilter,
        referralLimit: Int,
    ): LiveData<List<FollowUpPatientModel>> {
        val villageIds = if (filter.selectedVillages.isNullOrEmpty()) {
            filter.villages
        } else {
            filter.selectedVillages!!.map { it.id!! }
        }

        val fromAndToDate = getFromDateAndToDate(filter, referralLimit)

        val result = roomHelper.getFollowUpPatientListLiveData(
            filter.type,
            filter.search,
            villageIds,
            fromAndToDate.first,
            fromAndToDate.second,
        )

        val reasons = if (filter.type == FollowUpDefinedParams.FU_TYPE_MEDICAL_REVIEW) {
            emptyList()
        } else {
            filter.selectedReasons?.map { it.name }.orEmpty()
        }

        return result.map { items ->
            items
                .filter { item ->
                    reasons.isEmpty() || reasons.any { reason -> item.reason?.contains(reason, true) ?: true }
                }.sortedBy { it.updatedAt }
        }
    }

    private fun getFromDateAndToDate(
        filter: FollowUpFilter,
        referralLimit: Int,
    ): Pair<String, String> {
        if (filter.selectedDateRange.isNullOrEmpty()) {
            return Pair("", "")
        }

        if (filter.selectedDateRange?.any { it.name == FollowUpDefinedParams.FilterToday } == true) {
            val date = getTodayDateString(filter.type, referralLimit)
            return Pair(date, date)
        }

        if (filter.selectedDateRange?.any { it.name == FollowUpDefinedParams.FilterTomorrow } == true) {
            val date = getTomorrowDateString(filter.type, referralLimit)
            return Pair(date, date)
        }

        if (filter.selectedDateRange?.any { it.name == FollowUpDefinedParams.FilterCustomize } == true) {
            return getDateRange(filter, referralLimit)
        }

        return Pair("", "")
    }

    private fun getTodayDateString(
        type: String,
        referralLimit: Int,
    ): String {
        val format = DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMdd)
        var today = LocalDate.now().atStartOfDay()

        if (type == FollowUpDefinedParams.FU_TYPE_REFERRED) {
            today = today.minusDays(referralLimit.toLong())
        }

        return today.format(format)
    }

    private fun getTomorrowDateString(
        type: String,
        referralLimit: Int,
    ): String {
        val format = DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMdd)
        var tomorrow = LocalDate.now().atStartOfDay().plusDays(1)

        if (type == FollowUpDefinedParams.FU_TYPE_REFERRED) {
            tomorrow = tomorrow.minusDays(referralLimit.toLong())
        }

        return tomorrow.format(format)
    }

    private fun getDateRange(
        filter: FollowUpFilter,
        referralLimit: Int,
    ): Pair<String, String> {
        val outputFormat = DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMdd)
        val inputFormat = DateTimeFormatter.ofPattern(DATE_ddMMyyyy)
        var fromDate = LocalDate.parse(filter.fromDate, inputFormat)
        var toDate = LocalDate.parse(filter.toDate, inputFormat)

        if (filter.type == FollowUpDefinedParams.FU_TYPE_REFERRED) {
            fromDate = fromDate.minusDays(referralLimit.toLong())
            toDate = toDate.minusDays(referralLimit.toLong())
        }

        return Pair(fromDate.format(outputFormat), toDate.format(outputFormat))
    }

    suspend fun getVillageIds(): List<VillageEntity> = roomHelper.getAllVillageEntity()

    suspend fun getUnSyncedFollowUpCount(): Int = roomHelper.getUnSyncedFollowUpCount()

    suspend fun addCallHistory(
        maxSuccessfulCallLimit: Int,
        maxUnSuccessfulCallLimit: Int,
        informedCallAttempts: Int,
        followUpId: Long,
        callStatus: FollowUpCallStatus,
        patientStatus: String? = null,
        reason: String? = null,
    ) {
        val followUp = roomHelper.getFollowUpById(followUpId)
        followUp.syncStatus = OfflineSyncStatus.NotSynced
        followUp.attempts = followUp.attempts + 1

        val lat = SecuredPreference.getDouble(SecuredPreference.EnvironmentKey.CURRENT_LATITUDE.name)
        val lng = SecuredPreference.getDouble(SecuredPreference.EnvironmentKey.CURRENT_LONGITUDE.name)

        val callDetail = FollowUpCall(
            followUpId = followUpId,
            callDate = System.currentTimeMillis().convertToUtcDateTime(),
            duration = 0,
            attempts = followUp.attempts,
            status = callStatus,
            patientStatus = patientStatus,
            reason = reason,
            latitude = lat,
            longitude = lng,
        )

        var newFollowUp: FollowUp? = null
        if (callDetail.status == FollowUpCallStatus.SUCCESSFUL) {
            newFollowUp = handleSuccessCall(followUpId, followUp, callDetail, maxSuccessfulCallLimit, informedCallAttempts)
        } else {
            handleUnSuccessfulCall(followUpId, followUp, callDetail, maxUnSuccessfulCallLimit)
        }

        roomHelper.addCallHistory(followUp, callDetail, newFollowUp)
    }

    private suspend fun handleSuccessCall(
        id: Long,
        followUp: FollowUp,
        call: FollowUpCall,
        maxSuccessfulCallLimit: Int,
        informedCallAttempts: Int,
    ): FollowUp? {
        // Get All other followup with same reason
        call.patientStatus?.let {
            updatePatientStatus(it, id, followUp)
        }

        followUp.successfulAttempts = followUp.successfulAttempts + 1
        if (call.patientStatus == INFORMED || call.patientStatus == NOT_INFORMED) {
            if (followUp.successfulAttempts >= informedCallAttempts) {
                followUp.isCompleted = true
                roomHelper.updateOtherDuplicateTickets(id, followUp)
            }
        } else {
            if (followUp.successfulAttempts >= maxSuccessfulCallLimit) {
                followUp.isCompleted = true
                roomHelper.updateOtherDuplicateTickets(id, followUp)
            }
        }

        return null
    }

    private suspend fun updatePatientStatus(
        status: String,
        id: Long,
        followUp: FollowUp,
    ) {
        followUp.currentPatientStatus = status
        val currentTime = System.currentTimeMillis()
        followUp.updatedAt = currentTime
        followUp.calledAt = currentTime
        when (status.lowercase()) {
            ReferralStatus.Recovered.name.lowercase() -> {
                followUp.isCompleted = true
                roomHelper.updateDuplicateTicketsAsCompleted(id, followUp)
            }

            ReferralStatus.Referred.name.lowercase() -> {
                if (followUp.type == FollowUpDefinedParams.FU_TYPE_REFERRED) {
                    roomHelper.updateOnTreatmentStatus(id, followUp, currentTime)
                } else {
                    followUp.isCompleted = true
                    roomHelper.updateDuplicateTicketsAsCompleted(id, followUp)
                }
            }
            else -> {
                roomHelper.updateOnTreatmentStatus(id, followUp, currentTime)
            }
        }
    }

    private suspend fun handleUnSuccessfulCall(
        followUpId: Long,
        followUp: FollowUp,
        call: FollowUpCall,
        maxUnSuccessfulCallLimit: Int,
    ) {
        followUp.unsuccessfulAttempts = followUp.unsuccessfulAttempts + 1
        if (followUp.unsuccessfulAttempts >= maxUnSuccessfulCallLimit) {
            followUp.isCompleted = true
        }

        if (call.reason?.equals(FollowUpDefinedParams.WRONG_NUMBER, true) == true) {
            followUp.isWrongNumber = true
            followUp.isCompleted = followUp.type != FollowUpDefinedParams.FU_TYPE_HH_VISIT
            roomHelper.updateOtherFollowUpForWrongNumber(followUpId, followUp.memberId)
        }
    }
}
