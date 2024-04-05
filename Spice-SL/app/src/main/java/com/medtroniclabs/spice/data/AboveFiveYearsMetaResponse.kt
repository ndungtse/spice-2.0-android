package com.medtroniclabs.spice.data

data class AboveFiveYearsMetaResponse(
    val systemicExaminations : List<ExaminationsComplaintItems>,
    val presentingComplaints : List<ExaminationsComplaintItems >,
    val diseaseCategories : ArrayList<DiseaseCategoryItems>,
    val medicalSupplies: List<ExaminationsComplaintItems>,
    val cost: List<ExaminationsComplaintItems>,
    val patientStatus: List<ExaminationsComplaintItems>
)
