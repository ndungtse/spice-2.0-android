package com.medtroniclabs.spice.data.offlinesync.model

import com.google.gson.JsonElement

data class Assessment(
    val referenceId: Long,
    val householdId: String?,
    val memberId: String?,
    val assessmentType: String,
    val assessmentDetails: JsonElement,
    val patientId: String,
    val villageId: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val referred: Boolean,
    val referredDate: String? = null,
    val patientStatus: String,
    val referredReasons: String? = null,
    val provenance: ProvanceDto,
    val latitude: Double,
    val longitude: Double,
    val summary: JsonElement
)
