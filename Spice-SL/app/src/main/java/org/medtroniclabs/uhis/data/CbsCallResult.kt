package org.medtroniclabs.uhis.data

import org.medtroniclabs.uhis.appextensions.convertToUtcDateTime

data class CbsFollowUp(
    val followUpDetails: MutableList<CbsCallResult> = mutableListOf(),
)

data class CbsCallResult(
    val duration: Long,
    val attempts: Int = 0,
    val status: String? = null,
    val reason: String? = null,
    val callDate: String = System.currentTimeMillis().convertToUtcDateTime(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val callCompleted: Boolean = true,
    val callInitiated: Boolean = true,
)
