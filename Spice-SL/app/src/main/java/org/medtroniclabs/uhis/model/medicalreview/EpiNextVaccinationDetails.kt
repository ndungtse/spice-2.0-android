package org.medtroniclabs.uhis.model.medicalreview

data class EpiNextVaccinationDetails(
    val nextVaccinationDuration: String,
    val nextVaccinationDose: List<String>,
    var nextVisitDate: String,
)
