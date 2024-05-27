package com.medtroniclabs.spice.data

data class UnderFiveYearsMetaResponse(
    val systemicExaminations: ArrayList<MedicalReviewMetaItems>,
    val examinations : ArrayList<ExaminationListItems>
)