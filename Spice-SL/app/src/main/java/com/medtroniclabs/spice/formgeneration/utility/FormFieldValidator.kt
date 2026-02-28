package com.medtroniclabs.spice.formgeneration.utility

import android.util.Patterns
import java.util.regex.Pattern

object FormFieldValidator {
    /**
     * Regex that detects 5 consecutive identical digits in a phone number.
     *
     * Pattern: ([0-9])\1{4}
     *
     * Explanation:
     * - ([0-9])  → Captures any single digit (0–9)
     * - \1{4}    → Matches the same captured digit repeated 4 additional times
     *
     * This means it matches sequences like:
     * 00000, 11111, 77777, 99999
     *
     * Used to reject phone numbers containing unrealistic repeated digits
     * (e.g., 9999912345 or 1111100000).
     */
    private const val PHONE_NUMBER_REGEX = "([0-9])\\1{4}"

    fun isValidMobileNumber(mobileNumber: String): Boolean = Patterns.PHONE.matcher(mobileNumber).matches() && validatePhoneNumber(mobileNumber)

    private fun validatePhoneNumber(phoneNumber: String): Boolean {
        val pattern = Pattern.compile(PHONE_NUMBER_REGEX)
        val matcher = pattern.matcher(phoneNumber)
        if (matcher.find()) {
            return false
        }
        return true
    }
}
