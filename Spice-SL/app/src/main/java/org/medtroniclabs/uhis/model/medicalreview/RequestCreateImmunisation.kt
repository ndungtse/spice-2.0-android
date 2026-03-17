package org.medtroniclabs.uhis.model.medicalreview

import org.medtroniclabs.uhis.data.model.MedicalReviewEncounter

data class RequestCreateImmunisation(
    val immunisationList: MutableList<VaccinationDetail>,
    val encounter: MedicalReviewEncounter,
    val missedReason: String? = null,
)
