package com.medtroniclabs.spice.common

import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMdd
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.calculateAge
import com.medtroniclabs.spice.common.DateUtils.dateToMonths
import com.medtroniclabs.spice.formgeneration.config.DefinedParams
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateUtilsTest {

    @Test
    fun calculateBirthDate_yearsMonthsWeeks_returnStringDate() {
        val years = 24
        val months = 3
        val weeks = 3
        val result = DateUtils.calculateBirthDate(years, months, weeks)
        val expectedResult = ZonedDateTime.now().minusYears(24).minusMonths(3).minusWeeks(3)
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMddHHmmssZZZZZ))
        assert(expectedResult == result)
    }

    @Test
    fun getYearMonthAndWeek_stringDate_returnTripleDate() {
        val inputDate = "01-01-2000"
        val expected = Triple(24, 3, 1)
        val result = DateUtils.getYearMonthAndWeek(inputDate)
        assert(expected.first == result.first)
        assert(expected.second == result.second)
        assert(expected.third == result.third)
    }

    @Test
    fun getYearMonthAndWeek_stringDate_returnNull() {
        val inputDate = " "
        val expectedResult = null
        val result = DateUtils.getYearMonthAndWeek(inputDate)
        assert(expectedResult == result.first)
        assert(expectedResult == result.second)
        assert(expectedResult == result.third)
    }

    @Test
    fun getDateString_time_returnStringDate() {
        val time = Instant.now().toEpochMilli()
        val inputFormat = DATE_ddMMyyyy
        val outputFormat = DATE_FORMAT_yyyyMMddHHmmssZZZZZ
        val expectedResult = LocalDate.now()
            .format(DateTimeFormatter.ofPattern(DATE_ddMMyyyy)) + DefinedParams.DOBString
        val result = DateUtils.getDateString(time, inputFormat, outputFormat)
        assert(expectedResult == result)
    }

    @Test
    fun getDateString_time_returnInputFormatDate() {
        val time = Instant.now().toEpochMilli()
        val inputFormat = DATE_ddMMyyyy
        val outputFormat = DATE_ddMMyyyy
        val expectedResult = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_ddMMyyyy))
        val result = DateUtils.getDateString(time, inputFormat, outputFormat)
        assert(expectedResult == result)
    }

    @Test
    fun getYearMonthAndDate_stringDate_returnTripleDate() {
        val dateString = "08/04/2024"
        val result = DateUtils.getYearMonthAndDate(dateString)
        assert(2024 == result.first)
        assert(3 == result.second)
        assert(8 == result.third)
    }

    @Test
    fun getYearMonthAndDate_stringDate_returnNull() {
        val expectedResult = null
        val result = DateUtils.getYearMonthAndDate(" ")
        assert(expectedResult == result.first)
        assert(expectedResult == result.second)
        assert(expectedResult == result.third)
    }

    @Test
    fun calculateAge_birthYear_returnsAge() {
        val expectedResult = 24
        val actualResult = calculateAge(2000)
        assert(expectedResult == actualResult)
    }

    @Test
    fun calculateAgeInMonths_startDate_returnMonths() {
        val expectedResult = 3
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.MONTH, -3)
        val months = DateUtils.calculateAgeInMonths(calendar.time)
        assert(expectedResult == months)
    }

    @Test
    fun convertDateTimeToDate_date_returnStringDate() {
        val testDate =
            ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMddHHmmssZZZZZ))
        val inputFormat = DATE_FORMAT_yyyyMMddHHmmssZZZZZ
        val outputFormat = DATE_FORMAT_yyyyMMdd
        val expectedResult =
            ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMdd))
        val result = DateUtils.convertDateTimeToDate(testDate, inputFormat, outputFormat)
        assert(expectedResult == result)
    }

    @Test
    fun convertDateTimeToDate_date_returnConvertDateTimeToDate() {
        val inputText = ZonedDateTime.now()
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMddHHmmssZZZZZ))

        val sdfInput = SimpleDateFormat(DATE_FORMAT_yyyyMMddHHmmssZZZZZ, Locale.ENGLISH)
        val parseDate = sdfInput.parse(inputText)

        val sdfOutput1 = SimpleDateFormat(DATE_ddMMyyyy, Locale.ENGLISH)
        val expectedResult = parseDate?.let { sdfOutput1.format(it) }

        val inputFormat = DATE_FORMAT_yyyyMMddHHmmssZZZZZ
        val outputFormat = DATE_ddMMyyyy
        val result = DateUtils.convertDateTimeToDate(inputText, inputFormat, outputFormat)
        assert(expectedResult == result)
    }

    @Test
    fun convertDateTimeToDate_date_returnEmptyString() {
        val testDate = " "
        val inputFormat = DATE_FORMAT_yyyyMMddHHmmssZZZZZ
        val outputFormat = DATE_FORMAT_yyyyMMdd
        val expected = ""
        val result = DateUtils.convertDateTimeToDate(testDate, inputFormat, outputFormat)
        assert(expected == result)
    }


    @Test
    fun getDateAfterDays_days_returnNextDate() {

        val dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_ddMMyyyy)
        val now = LocalDate.now()
            .plusDays(1)
            .format(dateTimeFormatter)
        val day = DateUtils.getDateAfterDays(1)
        assert(now.toString() == day)
    }

    @Test
    fun getTomorrowDate_returnNextDateInMillis() {
        val dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_ddMMyyyy)
        val expectedResult = LocalDate.now().plusDays(1).format(dateTimeFormatter)
        val dateInMillis = DateUtils.getTomorrowDate()
        val actualResult =
            Instant.ofEpochMilli(dateInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                .format(dateTimeFormatter)
        assert(expectedResult == actualResult)
    }

    @Test
    fun convertddMMMToddMM_inputDate_returnConvertedDateTriple() {
        val convertedDateTriple = DateUtils.convertddMMMToddMM("21/09/1998")
        assert(convertedDateTriple.first == 1998)
        assert(convertedDateTriple.second == 8)
        assert(convertedDateTriple.third == 21)
    }

    @Test
    fun convertddMMMToddMM_inputDate_returnNull() {
        val convertedDateTriple = DateUtils.convertddMMMToddMM(" ")
        assert(convertedDateTriple.first == null)
        assert(convertedDateTriple.second == null)
        assert(convertedDateTriple.third == null)
    }

    @Test
    fun convertDateFormat_inputDateString_returnConvertedStringDate() {
        val expectedResult =
            LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_ddMMyyyy))
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_ddMMyyyy))
        val convertedStringDate = DateUtils.convertDateFormat(
            date,
            DATE_ddMMyyyy,
            DATE_FORMAT_ddMMyyyy
        )
        assert(expectedResult == convertedStringDate)
    }

    @Test
    fun convertDateFormat_invalidDate_returnEmptyString() {
        val expectedResultEmptyString = " "
        val convertedStringDateEmptyString = DateUtils.convertDateFormat(
            "",
            DATE_ddMMyyyy,
            DATE_FORMAT_ddMMyyyy
        )
        assert(expectedResultEmptyString == convertedStringDateEmptyString)
    }

    @Test
    fun dateToMonths_with_valid_returnDateString() {
        val testDate = ZonedDateTime.now().minusMonths(3)
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMddHHmmssZZZZZ))
        val expectedResult = 3
        val actualResult = dateToMonths(testDate)
        assert(expectedResult == actualResult)
    }

    @Test
    fun dateToMonths_with_valid_returnNull() {
        val expectedResult = null
        val actualResult = dateToMonths(" ")
        assert(expectedResult == actualResult)
    }

    @Test
    fun calculateEstimatedDeliveryDate_lastMenstrualDate_returnCalenderDate() {
        val lastMenstrualDate = Calendar.getInstance()
        val result = DateUtils.calculateEstimatedDeliveryDate(lastMenstrualDate)
        val expectedDate = Calendar.getInstance()
        expectedDate.add(Calendar.DAY_OF_MONTH, 280)
        val formatDate = SimpleDateFormat(DATE_ddMMyyyy)
        val resultFormatted = formatDate.format(result.time)
        val expectedDateFormatted = formatDate.format(expectedDate.time)
        assert(resultFormatted == expectedDateFormatted)

    }

    @Test
    fun calculateGestationalAge_lastMenstrualDate_returnCalenderDate() {
        val lastMenstrualDate = Calendar.getInstance()
        lastMenstrualDate.add(Calendar.DAY_OF_MONTH, -28)
        val gestationalAge = DateUtils.calculateGestationalAge(lastMenstrualDate)
        val expectedWeeks = 4L
        val expectedDays = 0L
        assert(expectedWeeks == gestationalAge.first)
        assert(expectedDays == gestationalAge.second)
    }

    @Test
    fun calculateAge_birtDate_returnInt() {

        val testDate = ZonedDateTime.now().minusYears(4)
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_yyyyMMddHHmmssZZZZZ))
        val expectedResult = 4
        val calculateAge = calculateAge(testDate)
        assert(expectedResult == calculateAge)
    }

    @Test
    fun calculateAge_birtDate_returnEmpty() {
        val expectedResult = 0
        val calculateAge = calculateAge(" ")
        assert(expectedResult == calculateAge)
    }

}