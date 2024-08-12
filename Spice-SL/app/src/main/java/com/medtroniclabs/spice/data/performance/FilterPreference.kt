package com.medtroniclabs.spice.data.performance

import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class FilterPreference(
    val userId: String? = null,
    val preference: Preference ? = null
)

data class Preference(
    val userIds: List<Long>? = null,
    val fhirIds: List<String>? = null,
    val villageIds: List<Long>? = null,
    val fromDate: String? = null,
    val toDate: String? = null
) {

    fun getYearAndMonth(): Pair<Int, Int> {
        val date = if (!this.fromDate.isNullOrEmpty()) LocalDate.parse(this.fromDate) else LocalDate.now()
        return Pair(date.year, date.monthValue - 1)
    }

    fun getFromToDate(): Pair<LocalDate, LocalDate> {
        val fromDate = if (!this.fromDate.isNullOrEmpty()) {
            LocalDate.parse(this.fromDate)
        } else {
            LocalDate.now().withDayOfMonth(1)
        }

        val toDate = if (!this.toDate.isNullOrEmpty()) {
            LocalDate.parse(this.toDate)
        } else {
            val currentDate = LocalDate.now()
            YearMonth.from(currentDate).atEndOfMonth()
        }

        return Pair(fromDate, toDate)
    }
}