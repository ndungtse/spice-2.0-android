package com.medtroniclabs.spice.data

data class PrescriptionListRequest(val patientReference: String, val isActive: Boolean = true)