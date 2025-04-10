package com.medtroniclabs.spice.data

data class TbMetaResponse(
    val presentingComplaints: List<MedicalReviewMetaItems>,
    val systemicExaminations: List<MedicalReviewMetaItems>,
    val comorbidities: List<MedicalReviewMetaItems>,
    val diseaseCategories : ArrayList<DiseaseCategoryItems>,
    val patientType : List<MedicalReviewMetaItems>,
    val patientStatus : List<MedicalReviewMetaItems>,
    val treatmentOutcome : List<MedicalReviewMetaItems>,
)
