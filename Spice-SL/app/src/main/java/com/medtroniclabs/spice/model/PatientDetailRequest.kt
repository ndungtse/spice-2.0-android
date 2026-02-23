package com.medtroniclabs.spice.model

import com.medtroniclabs.spice.common.CommonUtils

data class PatientDetailRequest(
    val id: String? = null,
    val patientId: String? = null,
    val ticketId: String? = null,
    val type: String? = null,
    val assessmentType: String? = null,
)

data class ReferralDetailRequest(
    val id: String? = null,
    val patientId: String? = null,
    val patientReference: String? = null,
    val patientVisitId: String? = null,
    val ticketId: String? = null,
    val encounterId: String? = null,
    val type: String? = null,
    val requestFrom: String? = CommonUtils.requestFrom(),
    val memberId: String? = null,
)
