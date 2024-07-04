package com.medtroniclabs.spice.common

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.nullIfEmpty
import com.medtroniclabs.spice.common.DateUtils.calculateAge
import com.medtroniclabs.spice.common.RoleConstant.CHA
import com.medtroniclabs.spice.common.RoleConstant.LAB_ASSISTANT
import com.medtroniclabs.spice.common.RoleConstant.MCHA
import com.medtroniclabs.spice.common.RoleConstant.MID_WIFE
import com.medtroniclabs.spice.common.RoleConstant.PROVIDER
import com.medtroniclabs.spice.common.RoleConstant.SECHN
import com.medtroniclabs.spice.common.RoleConstant.SRN
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.MONTH
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.MONTHS
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.WEEK
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.WEEKS
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.YEARS
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CommonUtils {
    fun checkIsTablet(context: Context): Boolean {
        val res = context.resources?.getBoolean(R.bool.isTablet)
        return res ?: false
    }

    fun getStringFromAssets(fileName: String, assets: AssetManager): String {
        return assets.open(fileName).bufferedReader().use { it.readText() }
    }

    fun getIntegerOrNull(answer: Any?): Int? {
        return if (answer is Int) {
            answer
        } else if (answer is String) {
            val answerNumber = answer.toIntOrNull()
            if (answerNumber is Int) {
                answerNumber
            } else {
                null
            }
        } else {
            null
        }
    }

    fun getLongOrNull(answer: Any?): Long? {
        when (answer) {
            is Long -> return answer
            is String -> {
                val answerNumber = answer.toLongOrNull()
                return if (answerNumber is Long) {
                    answerNumber
                } else {
                    null
                }
            }

            is Double -> {
                return answer.toLong()
            }

            else -> {
                return null
            }
        }
    }

    fun getStringOrEmptyString(answer: Any?): String {
        return if (answer is String) {
            answer
        } else ""
    }

    fun getIsBooleanFromString(answer: Any?): Boolean {
        return (answer is String) && answer.equals(HouseHoldRegistration.yes, true)
    }

    fun displayAge(dobString: String, context: Context): String {
        val yearMonthWeek = DateUtils.getV2YearMonthAndWeek(dobString)
        val years = yearMonthWeek.years
        var months = yearMonthWeek.months
        val weeks = yearMonthWeek.weeks

        if (months == 0 && years == 0 && weeks == 0) {
            return context.getString(R.string.separator_hyphen)
        }
        
        val strBuilder = StringBuilder()
        if (years < 5) {
            months += (years * 12)
        } else {
            strBuilder.append("$years $YEARS ")
            strBuilder.append(" ")
        }

        if (months > 0) {
            if (months == 1) {
                strBuilder.append("$months $MONTH")
                strBuilder.append(" ")
            } else {
                strBuilder.append("$months $MONTHS")
                strBuilder.append(" ")
            }
        }

        if (weeks > 0) {
            if (weeks == 1) {
                strBuilder.append("$weeks $WEEK")
                strBuilder.append(" ")
            } else {
                strBuilder.append("$weeks $WEEKS")
                strBuilder.append(" ")
            }
        }

        return strBuilder.toString()
    }

    fun getDuration(input: String, context: Context): String {
        val parts = input.split('/')

        return when {
            parts[0] == DefinedParams.ZERO && parts[1] == DefinedParams.ZERO -> context.getString(
                R.string.no_of_weeks,
                parts[2]
            )

            parts[0] == DefinedParams.ZERO -> context.getString(R.string.no_of_months, parts[1])
            else -> context.getString(R.string.no_of_years, parts[0])
        }
    }

    fun convertListToString(dispensedList: ArrayList<String>): String {
        return dispensedList.nullIfEmpty()?.joinToString(separator = ", ") ?: "-"
    }

    fun getYearMonthAndWeeks(dateString: String): Triple<String, String, String> {
        val parts = dateString.split("/")
        return Triple(parts[0], parts[1], parts[2])
    }

    fun getDurationInYMD(input: String, context: Context): String {
        val parts = input.split('/')

        return when {
            parts[0] == DefinedParams.ZERO && parts[1] == DefinedParams.ZERO -> context.getString(
                R.string.weeks_w,
                parts[2]
            )

            parts[0] == DefinedParams.ZERO -> context.getString(R.string.months_m, parts[1])
            else -> context.getString(R.string.years_y, parts[0])
        }
    }

    fun isProvider(): Boolean {
        return SecuredPreference.getRole() == PROVIDER
    }

    fun isChw(): Boolean {
        return SecuredPreference.getRole() == RoleConstant.COMMUNITY_HEALTH_WORKER
    }

    fun isRolePresent(): Boolean {
        val roleList =
            listOf(SECHN, MCHA, PROVIDER, CHA, MID_WIFE, LAB_ASSISTANT, SRN).map { it.lowercase() }
        val currentRole = SecuredPreference.getRole()?.lowercase()
        return roleList.contains(currentRole)
    }


    fun getOptionMap(value: String, name: String): Map<String, Any> {
        val map = HashMap<String, Any>()
        map[DefinedParams.ID] = value
        map[DefinedParams.NAME] = name
        return map
    }

    fun getAgeFromDOB(dateOfBirth: String?, context: Context): String {
        if (dateOfBirth != null) {
            val yearMonthWeek = DateUtils.getV2YearMonthAndWeek(dateOfBirth)

            if (yearMonthWeek.years >= 5) {
                return "${yearMonthWeek.years}"
            }

            val months = (yearMonthWeek.years * 12) + yearMonthWeek.months
            if (months > 0) {
                return if (months == 1) "$months ${context.getString(R.string.month)}" else "$months ${
                    context.getString(
                        R.string.months
                    )
                }"
            }

            if (yearMonthWeek.weeks > 0) {
                return if (yearMonthWeek.weeks == 1) "${yearMonthWeek.weeks} ${context.getString(R.string.week)}" else "${yearMonthWeek.weeks} ${
                    context.getString(
                        R.string.weeks
                    )
                }"
            }

            if (yearMonthWeek.days >= 0) {
                return if (yearMonthWeek.days < 2) "${yearMonthWeek.days} ${context.getString(R.string.day)}" else "${yearMonthWeek.days} ${
                    context.getString(
                        R.string.days
                    )
                }"
            }
        }
        return ""
    }

    fun getAgeFromDob(dateOfBirth: String?, month: String): String {
        if (dateOfBirth != null) {
            val age = calculateAge(dateOfBirth)
            return if (age != null && age > 5) {
                "$age"
            } else {
                val startDate = DateUtils.formatStringToDate(
                    dateOfBirth, SimpleDateFormat(
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        Locale.ENGLISH
                    )
                )
                startDate?.let { date ->
                    "${DateUtils.calculateAgeInMonths(date)} $month"
                } ?: kotlin.run {
                    return ""
                }
            }
        } else {
            return ""
        }
    }


    private fun calculateAge(dateOfBirth: String): Int? {
        val ageTriplet = DateUtils.getYearMonthAndDate(
            dateOfBirth, SimpleDateFormat(
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                Locale.ENGLISH
            )
        )
        val year = ageTriplet.first
        return year?.let { calculateAge(it) }
    }

    fun getGenderText(gender: String?, context: Context): String {
        return if (gender.equals(DefinedParams.male, true)) {
            context.getString(R.string.male_prefix)
        } else {
            context.getString(R.string.female_prefix)
        }
    }

    fun getBooleanAsString(value: Boolean): String {
        return if (value) HouseHoldRegistration.yes else HouseHoldRegistration.no
    }

    fun formatListToStringWithOther(list: List<String?>, otherText: String? = null): String {
        return when {
            list.filterNotNull().isNotEmpty() && otherText != null -> "${
                list.filterNotNull().joinToString(separator = ", ")
            } - $otherText"

            list.filterNotNull().isNotEmpty() -> list.filterNotNull().joinToString(separator = ", ")
            otherText != null -> otherText
            else -> "-"
        }
    }

    fun getBMI(heightInCM: Double, weight: Double, context: Context): String {
        val heightInMeter = heightInCM / 100
        val bmi = weight / (heightInMeter * heightInMeter)
        if (bmi.isInfinite() || bmi.isNaN()) {
            return context.getString(R.string.hyphen_symbol)
        }
        return getDecimalFormatted(bmi)
    }

    fun convertStringDobToMonths(dateOfBirth: String): Int? {
        val startDate = DateUtils.formatStringToDate(
            dateOfBirth, SimpleDateFormat(
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                Locale.ENGLISH
            )
        )
        return startDate?.let { date ->
            DateUtils.calculateAgeInMonths(date)
        }
    }

    fun isAlphabetsWithSpace(input: String): Boolean {
        return input.matches(Regex(RegexConstants.Contains_Alphabets_Space))
    }

    fun getDaysValue(enteredDays: String, maxDays: Int, context: Context): String {
        return try {
            val days = enteredDays.toDouble().toInt()
            if (days > maxDays) {
                context.getString(R.string.days_summary, days, maxDays + 1)
            } else {
                days.toString()
            }
        } catch (e: NumberFormatException) {
            enteredDays
        }
    }

    fun convertStringToIntString(value: String): String {
        return try {
            return value.toDouble().toInt().toString()
        } catch (e: NumberFormatException) {
            value
        }
    }

    fun getDecimalFormatted(value: Any?, pattern: String = "###.##"): String {
        var formattedValue = ""
        try {
            value?.let {
                val actualValue = if (it is String) it.toDoubleOrNull() ?: "" else it
                val df = DecimalFormat(pattern, DecimalFormatSymbols(Locale.ENGLISH))
                df.roundingMode = RoundingMode.FLOOR
                if (actualValue is String) {
                    if (actualValue.isNotBlank())
                        formattedValue = df.format(actualValue)
                } else
                    formattedValue = df.format(actualValue)
            }
        } catch (_: Exception) {
            //Exception - Catch block
        }
        return formattedValue
    }

    fun Double?.toIntOrEmptyString(): String {
        return this?.toInt().toString()
    }

    fun Double?.toDoubleOrEmptyString(): String {
        return if (this != null) {
            if (this == this.toInt().toDouble()) {
                this.toInt().toString()
            } else {
                this.toString()
            }
        } else {
            ""
        }
    }

    fun getFilePath(id: String, context: Context): File {
        val cw = ContextWrapper(context)
        val directory = cw.getDir(id, Context.MODE_PRIVATE)
        return File(directory, DefinedParams.SIGN_DIR)
    }

    fun getTicketType(menuType: String): String? {
        return when (menuType) {
            MedicalReviewTypeEnums.AboveFiveYears.name, MedicalReviewTypeEnums.UnderTwoMonths.name, MedicalReviewTypeEnums.UnderFiveYears.name -> {
                MedicalReviewTypeEnums.ICCM.name
            }

            MedicalReviewTypeEnums.ANC.name, MedicalReviewTypeEnums.PNC.name, MedicalReviewTypeEnums.LabourDelivery.name -> {
                MedicalReviewTypeEnums.RMNCH.name
            }

            else -> {
                null
            }
        }
    }

    fun createPrescription(prescriptions: List<Prescription>?, context: Context): String? {
        return prescriptions?.takeIf { it.isNotEmpty() }?.mapIndexed { index, prescription ->
            "${index + 1}. ${prescription.medicationName} / ${getPrescriptionFreq(prescription.frequency)} / ${
                dayPeriod(
                    prescription.prescribedDays,
                    context
                )
            }"
        }?.joinToString("\n")
    }

    private fun dayPeriod(prescribedDays: Long?, context: Context): String {
        return if (prescribedDays == 1L) {
            "$prescribedDays ${context.getString(R.string.day)}"
        } else {
            "$prescribedDays ${context.getString(R.string.days)}"
        }
    }

    fun getContactNumber(phNumber: String?): String? {
        return SecuredPreference.getPhoneNumberCode()?.let { code ->
            val formattedCode = if (!code.startsWith("+")) "+$code" else code
            "$formattedCode $phNumber"
        }
    }

    fun getPrescriptionFreq(days: Int?): String {
        return when (days) {
            1 -> MedicalReviewTypeEnums.OD.name
            2 -> MedicalReviewTypeEnums.BD.name
            3 -> MedicalReviewTypeEnums.TDS.name
            4 -> MedicalReviewTypeEnums.QDS.name
            else -> ""
        }
    }

    fun getMaxDateLimit(menstrualPeriod: Boolean, minDays: Int?): Long? {
        return if (menstrualPeriod) {
            DateUtils.calculateGestationPastMonths(System.currentTimeMillis(), 11)
        } else {
            if (minDays != null) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, -minDays)
                return calendar.timeInMillis
            } else {
                return minDays
            }
        }
    }

    fun getMaxDateLimit(menstrualPeriod: Boolean, minDays: Long?): Long? {
        return if (menstrualPeriod) {
            DateUtils.calculateGestationPastMonths(System.currentTimeMillis(), 11)
        } else {
            return minDays
        }
    }
}