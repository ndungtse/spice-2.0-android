package com.medtroniclabs.spice.data

import java.time.LocalDateTime

data class BirthHistoryResponse(
    val birthWeight: String?=null,
    val lastMenstrualPeriod: String?=null,
    val dateOfDelivery: String?=null,
    val breathingProblem: String?=null
)
