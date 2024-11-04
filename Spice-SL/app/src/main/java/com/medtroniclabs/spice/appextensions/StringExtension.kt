package com.medtroniclabs.spice.appextensions

fun String?.textOrHyphen(): String {
    return if (this.isNullOrBlank()) "-" else this
}

fun String?.textOrEmpty(): String {
    return if (this.isNullOrBlank()) "" else this
}