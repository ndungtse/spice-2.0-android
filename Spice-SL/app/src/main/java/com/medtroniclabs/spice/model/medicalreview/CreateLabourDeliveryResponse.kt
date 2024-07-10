package com.medtroniclabs.spice.model.medicalreview

data class CreateLabourDeliveryResponse(
    val childPatientReference: String,
    val patientReference: String,
    val neonateId: String,
    val motherId: String
)
