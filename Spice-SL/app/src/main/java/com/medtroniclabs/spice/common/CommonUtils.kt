package com.medtroniclabs.spice.common

import android.content.Context
import android.content.res.AssetManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.MONTHS
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Month
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.WEEKS
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Week
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.YEARS
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Year
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.ZERO
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration

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

    fun displayAge(resultHashMap: HashMap<String, Any>, context: Context): String {
        val years = resultHashMap[Year] as? Int ?: 0
        val months = resultHashMap[Month] as? Int ?: 0
        val weeks = resultHashMap[Week] as? Int ?: 0
        return if (years != ZERO) {
            return if (months != ZERO) "$years $YEARS $months $MONTHS" else "$years $YEARS"
        } else if (months != ZERO) {
            return "$months $MONTHS"
        } else if (weeks != ZERO) {
            return "$weeks $WEEKS"
        } else {
            context.getString(R.string.separator_hyphen)
        }
    }
}