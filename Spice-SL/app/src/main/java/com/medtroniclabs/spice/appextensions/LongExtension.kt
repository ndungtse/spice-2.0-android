package com.medtroniclabs.spice.appextensions
import com.medtroniclabs.spice.common.DateUtils.DATE_TIME_DISPLAY_FORMAT
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun Long.convertToUtcDateTime(): String {
    val instant = Instant.ofEpochMilli(this).atOffset(ZoneOffset.UTC)
    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    return formatter.format(instant)
}

fun Long.convertToLocalDateTime(format: String = DATE_TIME_DISPLAY_FORMAT): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    return dateFormat.format(date)
}