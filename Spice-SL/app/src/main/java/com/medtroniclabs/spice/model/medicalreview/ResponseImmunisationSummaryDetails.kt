package com.medtroniclabs.spice.model.medicalreview

data class ResponseImmunisationSummaryDetails(
    val vaccinated: List<String>,
    val missedVaccine: List<String>,
    val missedReason: String?,
    val lastScheduledDate: String,
    val lastType: String,
    val lastValue: String,
)
