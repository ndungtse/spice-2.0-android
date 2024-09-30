package com.medtroniclabs.spice.model

import com.medtroniclabs.spice.db.entity.LifestyleEntity
import com.medtroniclabs.spice.db.entity.NCDMedicalReviewMetaEntity

data class NcdMRStaticDataModel(
    val comorbidity: List<NCDMedicalReviewMetaEntity>,
    val complications: List<NCDMedicalReviewMetaEntity>,
    val lifestyle: List<LifestyleEntity>,
    val complaints: List<NCDMedicalReviewMetaEntity>,
    val physicalExamination: List<NCDMedicalReviewMetaEntity>,
    val currentMedication: List<NCDMedicalReviewMetaEntity>,
    val frequencies: List<NCDMedicalReviewMetaEntity>,
    val frequencyTypes: List<NCDMedicalReviewMetaEntity>
)
