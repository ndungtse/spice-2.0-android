package com.medtroniclabs.spice.data

import java.time.LocalDateTime

data class BirthHistoryResponse(
    val birthWeight: String?=null,

    val gestationalAge: Int?=null,
    val haveBreathingProblem: Boolean?=null
)
