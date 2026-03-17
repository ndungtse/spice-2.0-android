package org.medtroniclabs.uhis.ncd.data

import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto
import org.medtroniclabs.uhis.db.entity.TreatmentPlanEntity

data class NCDTreatmentPlanModel(
    val patientReference: String? = null,
    val memberReference: String? = null,
    val medicalReviewFrequency: TreatmentPlanEntity? = null,
    val bpCheckFrequency: TreatmentPlanEntity? = null,
    val bgCheckFrequency: TreatmentPlanEntity? = null,
    val hba1cCheckFrequency: TreatmentPlanEntity? = null,
    val choCheckFrequency: TreatmentPlanEntity? = null,
    val provenance: ProvanceDto = ProvanceDto(),
    val carePlanId: String? = null,
)

data class NCDTreatmentPlanModelDetails(
    val patientReference: String? = null,
    val memberReference: String? = null,
    val carePlanId: String? = null,
    val medicalReviewFrequency: String? = null,
    val bpCheckFrequency: String? = null,
    val bgCheckFrequency: String? = null,
    val hba1cCheckFrequency: String? = null,
    val choCheckFrequency: String? = null,
)
