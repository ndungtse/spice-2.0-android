package com.medtroniclabs.spice.ncd.followup

import com.medtroniclabs.spice.R

object NCDFollowUpUtils {
    const val SCREENED = "SCREENED"
    const val Assessment_Type = "ASSESSMENT"
    const val Defaulters_Type = "MEDICAL_REVIEW"
    const val LTFU_Type = "LOST_TO_FOLLOW_UP"
    const val visited_facility = "Visited facility"
    const val will_visit_facility = "Will visit facility"
    const val wont_visit_facility = "Won’t visit facility"

    fun getDaysString(it: Long): Int {
        return if (it == 1L) R.string.day_due else R.string.days_due
    }
}