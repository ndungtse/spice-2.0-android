package com.medtroniclabs.spice.data

data class MotherPncResponse(
    val systemicExaminations: List<MedicalReviewMetaItems>,
    val presentingComplaints: List<MedicalReviewMetaItems>,
    val diseaseCategories: List<Any>, // Assuming this can be any type or empty
    val dosageFrequencies: List<Any> // Assuming this can be any type or empty
)
data class NeonatePncResponse(
    val systemicExaminations: List<MedicalReviewMetaItems>,
    val presentingComplaints: List<MedicalReviewMetaItems>,
    val dosageFrequencies: List<Any> // Assuming this can be any type or empty
)

