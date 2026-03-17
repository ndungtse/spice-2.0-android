package org.medtroniclabs.uhis.data

data class SummaryCreateRequest(
    var motherDTO: MedicalReviewSummarySubmitRequest? = null,
    var neonateDTO: MedicalReviewSummarySubmitRequest? = null,
)
