package com.medtroniclabs.spice.model.medicalreview

import com.medtroniclabs.spice.data.EncounterDetails

data class RequestCreateImmunisation(
    val immunisationList : MutableList<VaccinationDetail>,
    val encounter: EncounterDetails,
    val missedReason: String? = null
)
