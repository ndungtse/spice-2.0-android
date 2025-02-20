package com.medtroniclabs.spice.model.medicalreview

data class EpiNextVaccinationDetails(
    val nextVaccinationDuration: String,
    val nextVaccinationDose: List<String>,
    var nextVisitDate: String
)
