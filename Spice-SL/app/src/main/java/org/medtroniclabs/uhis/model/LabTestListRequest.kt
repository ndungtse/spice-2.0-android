package org.medtroniclabs.uhis.model

data class LabTestListRequest(val patientReference: String, val roleName: String? = null)
