package com.medtroniclabs.spice.model

import com.medtroniclabs.spice.db.entity.ComorbidityEntity
import com.medtroniclabs.spice.db.entity.ComplaintsEntity
import com.medtroniclabs.spice.db.entity.ComplicationsEntity
import com.medtroniclabs.spice.db.entity.CurrentMedicationEntity
import com.medtroniclabs.spice.db.entity.LifestyleEntity
import com.medtroniclabs.spice.db.entity.PhysicalExaminationEntity
import com.medtroniclabs.spice.db.entity.TreatmentPlanEntity
import com.medtroniclabs.spice.db.entity.TreatmentPlanFrequencyEntity

data class NcdMRStaticDataModel(
    val comorbidity: List<ComorbidityEntity>,
    val complications: List<ComplicationsEntity>,
    val lifestyle: List<LifestyleEntity>,
    val complaints: List<ComplaintsEntity>,
    val physicalExamination: List<PhysicalExaminationEntity>,
    val currentMedication: List<CurrentMedicationEntity>,
    val frequencies: List<TreatmentPlanFrequencyEntity>,
    val frequencyTypes: List<TreatmentPlanEntity>
)
