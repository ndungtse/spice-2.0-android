package com.medtroniclabs.spice.common

import com.medtroniclabs.spice.formgeneration.config.DefinedParams
import org.joda.time.Period
import org.joda.time.PeriodType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    const val DATE_ddMMyyyy = "dd/MM/yyyy"
    const val DATE_FORMAT_ddMMyyyy = "dd-MM-yyyy"
    const val DATE_FORMAT_yyyyMMdd = "yyyy-MM-dd"
    const val DATE_FORMAT_yyyyMMddHHmmssZZZZZ = "yyyy-MM-dd'T'HH:mm:ssZZZZZ"

    fun getYearMonthAndWeek(inputDate: String): Triple<Int?, Int?, Int?> {
        try {
            val dateFormat = SimpleDateFormat(DATE_FORMAT_ddMMyyyy, Locale.getDefault())
            val birthDate = dateFormat.parse(inputDate)

            val currentDate = Calendar.getInstance().time

            val diff = currentDate.time - birthDate.time
            val age = Calendar.getInstance().apply {
                timeInMillis = diff
            }

            val years = age.get(Calendar.YEAR) - 1970
            val months = age.get(Calendar.MONTH)
            val weeks = (age.get(Calendar.DAY_OF_MONTH) - 1) / 7

            return Triple(years, months, weeks)
        } catch (exception: Exception) {
            return Triple(null, null, null)
        }
    }

    fun getYearMonthAndDate(
        dateString: String,
        defaultFormat: SimpleDateFormat = getDateDDMMYYYY()
    ): Triple<Int?, Int?, Int?> {
        try {
            val date = formatStringToDate(dateString, defaultFormat)
            date?.let {
                val cal = Calendar.getInstance()
                cal.time = date
                return Triple(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DATE)
                )
            }
        } catch (exception: Exception) {
            return Triple(null, null, null)
        }
        return Triple(null, null, null)
    }

    fun formatStringToDate(dateString: String, format: SimpleDateFormat): Date? {
        return format.parse(dateString)
    }

    fun getDatePatternDDMMYYYY() = SimpleDateFormat(DATE_FORMAT_ddMMyyyy, Locale.ENGLISH)

    fun getDateDDMMYYYY(): SimpleDateFormat {
        return SimpleDateFormat(DATE_ddMMyyyy, Locale.ENGLISH)
    }

    fun getDateString(time: Long, inputFormat: String?, outputFormat: String?): String {
        val date = Date(time)
        val format = SimpleDateFormat(
            inputFormat,
            Locale.ENGLISH
        )
        return format.format(date).let {
            if (outputFormat == DATE_FORMAT_yyyyMMddHHmmssZZZZZ) {
                "${format.format(date)}${DefinedParams.DOBString}"
            } else
                format.format(date)
        }
    }

    fun calculateBirthDate(years: Int, months: Int, weeks: Int): String {
        val days = weeks * 7

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -years)
        calendar.add(Calendar.MONTH, -months)
        calendar.add(Calendar.DAY_OF_MONTH, -days)

        val dateFormat = SimpleDateFormat(DATE_FORMAT_yyyyMMddHHmmssZZZZZ, Locale.ENGLISH)
        return dateFormat.format(calendar.time)
    }

    fun calculateAge(birthYear: Int): Int {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return currentYear - birthYear
    }

    fun calculateAgeInMonths(startDate: Date): Int {
        val endDate = Calendar.getInstance().time
        val period = Period(startDate.time, endDate.time, PeriodType.months())
        return period.months
    }


    fun getTomorrowDate(): Long {
        val chosenDate = Calendar.getInstance()
        chosenDate.timeInMillis = System.currentTimeMillis()
        chosenDate.add(Calendar.DAY_OF_MONTH, 1)
        return chosenDate.timeInMillis
    }

    fun convertDateTimeToDate(
        inputText: String?,
        inputFormat: String,
        outputFormat: String,
        inUserTimeZone: Boolean? = false,
        inUTC: Boolean? = null
    ): String {
        try {
            inputText?.let {
                if (it.isNotBlank()) {
                    var userTimeZone: TimeZone? = null
                    val isTimeZoneFormat = inputFormat == DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    if (isTimeZoneFormat || inUserTimeZone == true) {
                        getTimeZoneInput(inputText, isTimeZoneFormat)?.let { timeZone ->
                            userTimeZone = timeZone
                        }
                    } else if (inUTC == true) {
                        userTimeZone = getUTCFormat()
                    }
                    val sdfInput = SimpleDateFormat(inputFormat, Locale.ENGLISH)
                    userTimeZone?.let {
                        sdfInput.timeZone = userTimeZone
                    }
                    val date = sdfInput.parse(it)
                    date?.let {
                        val sdfOutput = SimpleDateFormat(outputFormat, Locale.ENGLISH)
                        userTimeZone?.let {
                            sdfOutput.timeZone = userTimeZone
                        }
                        return sdfOutput.format(date)
                    }
                }
            }
        } catch (e: Exception) {
            return ""
        }
        return ""
    }

    private fun getTimeZoneInput(inputText: String, timeZoneFormat: Boolean): TimeZone? {
        var timeZoneInput = "GMT+00:00" // Take from secured preference in future
        if (timeZoneInput.isNullOrBlank() && timeZoneFormat) {
            timeZoneInput = "GMT${
                if (inputText.contains("+")) inputText.substring(inputText.indexOf("+"))
                else inputText.substring(inputText.indexOf("-"))
            }"
        } else if (!timeZoneInput.isNullOrBlank())
            timeZoneInput = "GMT$timeZoneInput"
        return TimeZone.getTimeZone(timeZoneInput)
    }

    private fun getUTCFormat(): TimeZone? {
        return TimeZone.getTimeZone("GMT+00:00")
    }

    fun convertddMMMToddMM(inputDate: String): Triple<Int?, Int?, Int?> {
        return try {
            val inputFormat = SimpleDateFormat(DATE_ddMMyyyy, Locale.ENGLISH)
            val outputFormat = SimpleDateFormat(DATE_ddMMyyyy, Locale.ENGLISH)
            val date = inputFormat.parse(inputDate)
            getYearMonthAndDate(outputFormat.format(date))
        } catch (exception: Exception) {
            Triple(null, null, null)
        }
    }

}