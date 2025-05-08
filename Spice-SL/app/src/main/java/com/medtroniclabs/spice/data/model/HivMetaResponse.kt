package com.medtroniclabs.spice.data.model

import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.MedicalReviewMetaItems

data class HivMetaResponse(
    val patientStatus: List<MedicalReviewMetaItems>,
    val diseaseCategories: ArrayList<DiseaseCategoryItems>,
    val comorbidities: List<MedicalReviewMetaItems>,
    val populationType: List<MedicalReviewMetaItems>,
    val hivHistory: List<MedicalReviewMetaItems>,
    val hivTestDurations: List<MedicalReviewMetaItems>,
    val entryPoint: List<MedicalReviewMetaItems>,
    val whoClinicalStage: List<MedicalReviewMetaItems>,
    )