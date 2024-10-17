package com.medtroniclabs.spice.ncd.counseling.model

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class NCDCounselingModel(
    val id: String? = null,
    val patientReference: String? = null,
    val memberReference: String? = null,
    val visitId: String? = null,
    val provenance: ProvanceDto = ProvanceDto(),
    val roleName: String? = null,
    val lifestyles: List<String>? = null,
    val clinicianNote: String? = null,
    val lifestyleAssessment: String? = null,
    val otherNote: String? = null,
    val nutritionist: Boolean? = null,
    val clinicianNotes: List<String>? = null,
    val counselorAssessments: String? = null,
    val counselor: Boolean? = null,
    val referredDate: String? = null,
    val referredBy: String? = null,
    val referredByDisplay: String? = null,
    val assessedDate: String? = null,
    val assessedBy: String? = null,
    val assessedByDisplay: String? = null,
    var isExpanded: Boolean = false
)
