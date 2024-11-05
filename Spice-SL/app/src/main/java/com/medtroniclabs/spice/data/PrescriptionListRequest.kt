package com.medtroniclabs.spice.data


data class PrescriptionListRequest(val patientReference: String? = null, val isActive: Boolean = false, val requestFrom : String? = null)



