package com.medtroniclabs.spice.appextensions
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun Long.convertToUtcDateTime(): String {
    val instant = Instant.ofEpochMilli(this).atOffset(ZoneOffset.UTC)
    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    return formatter.format(instant)
}