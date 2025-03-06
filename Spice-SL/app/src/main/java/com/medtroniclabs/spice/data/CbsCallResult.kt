package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.appextensions.convertToUtcDateTime

data class CbsFollowUp (
    val followUpDetails: MutableList<CbsCallResult> = mutableListOf()
)

data class CbsCallResult(
    val status: String? = null,
    val reason: String? = null,
    val callDate: String =  System.currentTimeMillis().convertToUtcDateTime()
)
