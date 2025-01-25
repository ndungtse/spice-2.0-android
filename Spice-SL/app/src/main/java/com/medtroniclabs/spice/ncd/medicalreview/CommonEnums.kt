package com.medtroniclabs.spice.ncd.medicalreview

import com.medtroniclabs.spice.R

enum class CommonEnums(val title: String, val cultureValue: Int, val value: String) {
    YESTERDAY("Yesterday", R.string.yesterday, "yesterday"),
    TODAY("Today", R.string.today, "today"),
    TOMORROW("Tomorrow", R.string.tomorrow, "tomorrow"),
    REFERRED("Referred", R.string.referred, "referred"),
    ON_TREATMENT("On Treatment", R.string.on_treatment, "on treatment"),
    ENROLLED("Registered", R.string.registered, "ENROLLED"),
    NOT_ENROLLED("Not Registered", R.string.not_registered, "NOT_ENROLLED"),
    HIGH_RISK("High", R.string.high, "high risk"),
    MEDIUM_RISK("Medium", R.string.medium, "medium risk"),
    LOW_RISK("Low", R.string.low, "low risk"),
    RED_RISK("Red Risk", R.string.red_risk, "red risk"),
    WEEK("Red Risk", R.string.this_week, "week"),
    MONTH("Red Risk", R.string.this_month, "month"),
    CUSTOMISE("Red Risk", R.string.customize, "customize")
}