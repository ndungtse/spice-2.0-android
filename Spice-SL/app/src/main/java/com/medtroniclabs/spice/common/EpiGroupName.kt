package com.medtroniclabs.spice.common

object EpiGroupName {
    fun getGroupName(
        value: Int,
        type: String,
    ): String =
        when {
            type == "MONTH" -> "$value Months"
            value == 0 -> "At Birth"
            else -> "$value Weeks"
        }
}
