package com.medtroniclabs.spice.common

import android.content.Context
import android.content.res.AssetManager
import android.widget.TextView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.MONTHS
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Month
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.WEEKS
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Week
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.YEARS
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Year
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.ZERO
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

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
                    return answerNumber
                } else {
                    return null
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

    fun displayAge(resultHashMap: HashMap<String, Any>, context: Context): String? {
        val years = resultHashMap[Year] as? Int ?: 0
        var months = resultHashMap[Month] as? Int ?: 0
        var weeks = resultHashMap[Week] as? Int ?: 0

        if (months == 0 && years == 0 && weeks == 0) {
            return context.getString(R.string.separator_hyphen)
        }
        val strBuilder = StringBuilder()

        if (years >= 5)
            strBuilder.append("$years $YEARS ")
        else {
            if (years > 0) {
                months += (years * 12) + (weeks / 4)
                weeks = 0
            }
        }

        if (months > 0)
            strBuilder.append("$months $MONTHS ")

        if (weeks > 0)
            strBuilder.append("$weeks $WEEKS")

        return strBuilder.toString()
    }

    fun getDuration(input: String, context: Context): String {
        val parts = input.split('/')

        return when {
            parts[0] == DefinedParams.ZERO && parts[1] == DefinedParams.ZERO -> context.getString(R.string.no_of_weeks, parts[2])
            parts[0] == DefinedParams.ZERO -> context.getString(R.string.no_of_months, parts[1])
            else -> context.getString(R.string.no_of_years, parts[0])
        }
    }

    fun convertListToString(dispensedList: ArrayList<String>): String {
        return dispensedList.joinToString(separator = ", ")
    }

    fun getYearMonthAndWeeks (dateString: String): Triple<String, String, String> {
        val parts = dateString.split("/")
        return Triple(parts[0], parts[1], parts[2])
    }

    fun calculateAgeString(map: HashMap<String, Any>): String {
        val year = map[Year]
        val month = map[Month]
        val week = map[Week]
        return "${year?.toString() ?: ""}/${month?.toString() ?: ""}/${week?.toString() ?: ""}"
    }

    fun getDurationInYMD(input: String, context: Context): String {
        val parts = input.split('/')

        return when {
            parts[0] == DefinedParams.ZERO && parts[1] == DefinedParams.ZERO -> context.getString(R.string.weeks_w, parts[2])
            parts[0] == DefinedParams.ZERO -> context.getString(R.string.months_m, parts[1])
            else -> context.getString(R.string.years_y, parts[0])
        }
    }

}