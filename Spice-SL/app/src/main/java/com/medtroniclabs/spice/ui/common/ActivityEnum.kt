package com.medtroniclabs.spice.ui.common

enum class ActivityEnum(val title: String, val fieldName: String) {
    TODAY("Today", "today"),
    YESTERDAY("Yesterday", "yesterday"),
    WEEK("This Week", "week"),
    MONTH("This Month", "month"),
    CUSTOMISE("Customize", "customize")
}