package com.medtroniclabs.spice.model.medicalreview

data class HivVitalsRequest(
    val patientReference: String? = null,
    val memberId: String? = null,
    val types: List<String>? = null,
)

data class HivVitalsResponse(
    val cd4: Double? = null,
    val whoClinicalStage: String? = null,
    val emtctVisitStatus: String? = null,
    val weight : Double? = null,
    val height : Double? = null,
    val cd4Percentage : Double? = null
)
