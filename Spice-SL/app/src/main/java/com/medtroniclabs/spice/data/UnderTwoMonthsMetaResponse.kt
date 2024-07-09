package com.medtroniclabs.spice.data

data class UnderTwoMonthsMetaResponse(
    val diseaseCategories : ArrayList<DiseaseCategoryItems>,
    val examinations : ArrayList<ExaminationListItems>,
    val patientStatus: ArrayList<MedicalReviewMetaItems>,
    val immunisationStatus: ArrayList<MedicalReviewMetaItems>
)
