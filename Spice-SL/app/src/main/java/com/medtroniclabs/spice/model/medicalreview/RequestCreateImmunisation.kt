package com.medtroniclabs.spice.model.medicalreview

import com.medtroniclabs.spice.data.model.MedicalReviewEncounter

data class RequestCreateImmunisation(
    val immunisationList: MutableList<VaccinationDetail>,
    val encounter: MedicalReviewEncounter,
    val missedReason: String? = null,
)
