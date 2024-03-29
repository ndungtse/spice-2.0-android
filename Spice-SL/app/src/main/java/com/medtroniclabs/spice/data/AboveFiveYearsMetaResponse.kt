package com.medtroniclabs.spice.data

data class AboveFiveYearsMetaResponse(
    val systemicExaminations : List<ExaminationsComplaintItems>,
    val presentingComplaints : List<ExaminationsComplaintItems >,
    val diseaseCategories : ArrayList<DiseaseCategoryItems>
)
