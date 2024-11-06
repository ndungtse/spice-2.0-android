package com.medtroniclabs.spice.appextensions

fun String?.validatedString(default:String = "-"): String {
    return if (this.isNullOrBlank()) default else this.trim()
}