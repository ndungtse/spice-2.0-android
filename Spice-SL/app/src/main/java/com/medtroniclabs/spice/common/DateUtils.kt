package com.medtroniclabs.spice.common

import com.medtroniclabs.spice.formgeneration.config.DefinedParams
import org.joda.time.Period
import org.joda.time.PeriodType
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object DateUtils {

    const val DATE_ddMMyyyy = "dd/MM/yyyy"
    const val DATE_FORMAT_ddMMyyyy = "dd-MM-yyyy"
    const val DATE_FORMAT_yyyyMMdd = "yyyy-MM-dd"
    const val DATE_FORMAT_yyyyMMddHHmmssZZZZZ = "yyyy-MM-dd'T'HH:mm:ssZZZZZ"
    const val DATE_FORMAT_ddMMMyyyy = "dd MMM, yyyy"

    fun getYearMonthAndWeek(
        inputDate: String,
        inputFormat: String = DATE_FORMAT_ddMMyyyy
    ): Triple<Int?, Int?, Int?> {
        try {
            val dateFormat = SimpleDateFormat(inputFormat, Locale.getDefault())
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

    fun getDateString(time: Long, inputFormat: String?=null, outputFormat: String?=null): String {
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

    fun calculateAgeInMonths(birthDateString: String): Pair<Int, Date>? {
        val startDate = formatStringToDate(
            birthDateString, SimpleDateFormat(
                DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                Locale.ENGLISH
            )
        )
        startDate?.let {
           return Pair(calculateAgeInMonths(it),it)
        }
        return null
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

    fun convertDateToLong(date: String?, format: String? = DATE_FORMAT_yyyyMMddHHmmssZZZZZ): Long? {
        try{
            val testedDate = date?.let{dateStr ->
                SimpleDateFormat(
                    format,
                    Locale.ENGLISH
                ).parse(dateStr)?.time
            }
            return testedDate ?: 0L
        }catch (e: Exception){
            return null
        }
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
                    val isTimeZoneFormat = inputFormat == DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    if (isTimeZoneFormat || inUserTimeZone == true) {
                        getTimeZoneInput(inputText, isTimeZoneFormat)?.let { timeZone ->
                            userTimeZone = timeZone
                        }
                    } else if (inUTC == true) {
                        userTimeZone = getUTCFormat()
                    }
                    val sdfInput = SimpleDateFormat(inputFormat, Locale.ENGLISH)
                    userTimeZone?.let { timeZone ->
                        sdfInput.timeZone = timeZone
                    }
                    val date = sdfInput.parse(it)
                    date?.let {
                        val sdfOutput = SimpleDateFormat(outputFormat, Locale.ENGLISH)
                        userTimeZone?.let { timeZone ->
                            sdfOutput.timeZone = timeZone
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
        if (timeZoneInput.isBlank() && timeZoneFormat) {
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

    fun convertedMMMToddMM(inputDate: String): Triple<Int?, Int?, Int?> {
        return try {
            val inputFormat = SimpleDateFormat(DATE_ddMMyyyy, Locale.ENGLISH)
            val outputFormat = SimpleDateFormat(DATE_ddMMyyyy, Locale.ENGLISH)
            val date = inputFormat.parse(inputDate)
            getYearMonthAndDate(outputFormat.format(date))
        } catch (exception: Exception) {
            Triple(null, null, null)
        }
    }

    fun convertDateFormat(
        inputDateString: String,
        inputDateFormat: String,
        outputDateFormat: String
    ): String {
        val inputFormat = SimpleDateFormat(inputDateFormat, Locale.getDefault())

        return try {
            val date = inputFormat.parse(inputDateString)
            val outputFormat = SimpleDateFormat(outputDateFormat, Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: " "
        } catch (e: Exception) {
            " "
        }
    }

    fun dateToMonths(
        dateString: String,
        givenFormat: String = DATE_FORMAT_yyyyMMddHHmmssZZZZZ
    ): Int? {
        return try {
            val formatter = DateTimeFormatter.ofPattern(givenFormat)
            val givenDateTime = LocalDateTime.parse(dateString, formatter)
            val currentDateTime = LocalDateTime.now()
            val months =
                givenDateTime.toLocalDate().until(currentDateTime.toLocalDate()).toTotalMonths()
            months.toInt()
        } catch (e: Exception) {
            null
        }
    }

    fun calculateAge(
        birthDateString: String,
        givenFormat: String = DATE_FORMAT_yyyyMMddHHmmssZZZZZ
    ): Int {
        return try {
            val yearConvert = 1000L * 60 * 60 * 24 * 365.25
            val dateFormat = SimpleDateFormat(givenFormat, Locale.getDefault())
            val birthDate = dateFormat.parse(birthDateString)
            val today = Calendar.getInstance().time
            val diff = today.time - birthDate.time
            val age = diff / yearConvert
            age.toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun getDateAfterDays(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)

        val dateFormat = SimpleDateFormat(DATE_ddMMyyyy, Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    fun calculateGestationalAge(lastMenstrualDate: Calendar): Pair<Long, Long> {
        val currentDate = Calendar.getInstance()
        val diffInMillis = currentDate.timeInMillis - lastMenstrualDate.timeInMillis
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
        val weeks = diffInDays / 7
        val days = diffInDays % 7
        return Pair(weeks, days)
    }

    fun calculateEstimatedDeliveryDate(lastMenstrualDate: Calendar): Calendar {
        val estimatedDeliveryDate = lastMenstrualDate.clone() as Calendar
        estimatedDeliveryDate.add(Calendar.DAY_OF_YEAR, 280)
        return estimatedDeliveryDate
    }

    fun getCurrentDateAndTime(format: String): String {
        val currentDateTime = ZonedDateTime.now(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern(format)
        return currentDateTime.format(formatter)
    }

    fun addDaysToDate(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_MONTH, days)
        return calendar.time
    }

    fun addMonthsToDate(date: Date, months: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MONTH, months)
        return calendar.time
    }

    fun convertStringToDate(dateString: String, format: String): Date? {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.parse(dateString)
    }

    fun getDateStringFromDate(date: Date, format: String): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(date)
    }

    fun daysBetweenDates(startDate: Date, endDate: Date): Long {
        val diffInMillis = abs(endDate.time - startDate.time)
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
    }

    fun getTodayDateDDMMYYYY(): String {
        val calendar = Calendar.getInstance()
        return getDateString(calendar.time.time, DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
    }
}