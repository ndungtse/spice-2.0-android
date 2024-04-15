package com.medtroniclabs.spice.data

data class UnderTwoMonthsMetaResponse(
    val diseaseCategories : ArrayList<DiseaseCategoryItems>,
    val examinations : ArrayList<ExaminationListItems>
)
