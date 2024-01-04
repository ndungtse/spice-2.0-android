package com.medtroniclabs.spice.common

import android.util.Patterns
import java.util.regex.Pattern

object Validator {

    private const val PHONE_NUMBER_REGEX = "([0-9])\\1{4}"

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidMobileNumber(mobileNumber: String): Boolean {
        return Patterns.PHONE.matcher(mobileNumber).matches() && validatePhoneNumber(mobileNumber)
    }

    private fun validatePhoneNumber(phoneNumber: String): Boolean {
        val pattern = Pattern.compile(PHONE_NUMBER_REGEX)
        val matcher = pattern.matcher(phoneNumber)
        if (matcher.find()) {
            return false
        }
        return true
    }
}