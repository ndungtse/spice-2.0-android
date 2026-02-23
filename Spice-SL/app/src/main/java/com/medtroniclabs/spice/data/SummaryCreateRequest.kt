package com.medtroniclabs.spice.data

data class SummaryCreateRequest(
    var motherDTO: MedicalReviewSummarySubmitRequest? = null,
    var neonateDTO: MedicalReviewSummarySubmitRequest? = null,
)
