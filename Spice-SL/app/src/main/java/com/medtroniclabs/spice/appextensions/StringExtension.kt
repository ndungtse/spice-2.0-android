package com.medtroniclabs.spice.appextensions

fun String?.textOrHyphen(): String {
    return if (this.isNullOrBlank()) "-" else this.trim()
}

fun String?.textOrEmpty(): String {
    return if (this.isNullOrBlank()) "" else this.trim()
}

fun Int?.numberOrZero(): Int {
    return if (this == null || this < 0) 0 else this
}
