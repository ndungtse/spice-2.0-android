package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class SummaryCreateRequest(
    var motherDTO: MedicalReviewSummarySubmitRequest?=null,
    var neonateDTO: MedicalReviewSummarySubmitRequest?=null)


