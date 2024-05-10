package com.medtroniclabs.spice.repo

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.followup.FollowUpFilter
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.ui.followup.FollowUpDefinedParams
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
        )
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
}