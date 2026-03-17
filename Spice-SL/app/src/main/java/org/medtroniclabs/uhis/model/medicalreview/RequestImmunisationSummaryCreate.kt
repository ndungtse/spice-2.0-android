package org.medtroniclabs.uhis.model.medicalreview

import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto

data class RequestImmunisationSummaryCreate(
    val vaccinated: List<String>,
    val missedVaccine: List<String>,
    val missedReason: String?,
    val lastScheduledDate: String,
    val lastScheduledDateReason: String,
    val encounterId: String,
    val memberId: String?,
    val patientId: String?,
    val nextVaccinationDuration: String?,
    val nextVaccinationDose: List<String>?,
    val nextVaccinationDate: String?,
    val provenance: ProvanceDto,
    val villageId: String?,
    val patientReference: String?,
)
