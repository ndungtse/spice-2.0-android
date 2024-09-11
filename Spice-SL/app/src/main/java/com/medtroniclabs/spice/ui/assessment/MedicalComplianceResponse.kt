package com.medtroniclabs.spice.ui.assessment

data class MedicalComplianceResponse(
    val name: String,
    val otherCompliance: String? = null,
    val cultureValue: String? = null
)
