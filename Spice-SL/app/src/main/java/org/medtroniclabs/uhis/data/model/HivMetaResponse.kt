package org.medtroniclabs.uhis.data.model

import org.medtroniclabs.uhis.data.DiseaseCategoryItems
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems

data class HivMetaResponse(
    val patientStatus: List<MedicalReviewMetaItems>,
    val diseaseCategories: ArrayList<DiseaseCategoryItems>,
    val comorbidities: List<MedicalReviewMetaItems>,
    val presentingComplaints: List<MedicalReviewMetaItems>,
    val systemicExaminations: List<MedicalReviewMetaItems>,
    val populationType: List<MedicalReviewMetaItems>,
    val hivHistory: List<MedicalReviewMetaItems>,
    val hivPreganancyBreastFeedingStatus: List<MedicalReviewMetaItems>,
    val ahdStatus: List<MedicalReviewMetaItems>,
    val dsdStatus: List<MedicalReviewMetaItems>,
    val nonEstablishedModels: List<MedicalReviewMetaItems>,
    val establishedModels: List<MedicalReviewMetaItems>,
    val hivTestDurations: List<MedicalReviewMetaItems>,
    val entryPoint: List<MedicalReviewMetaItems>,
    val whoClinicalStage: List<MedicalReviewMetaItems>,
    val emtctVisitStatus: List<MedicalReviewMetaItems>,
    val emtctEntryPoint: List<MedicalReviewMetaItems>,
    val tbStatus: List<MedicalReviewMetaItems>,
    val maternalOutcome: List<MedicalReviewMetaItems>,
    val obstetricExaminations: List<MedicalReviewMetaItems>,
)
