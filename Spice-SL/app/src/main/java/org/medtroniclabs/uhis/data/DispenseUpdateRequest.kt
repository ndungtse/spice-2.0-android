package org.medtroniclabs.uhis.data

data class DispenseUpdateRequest(val patientVisitId: String? = null, val patientReference: String? = null, val isActive: Boolean? = false, val requestFrom: String? = null)

data class DispenseUpdateResponse(val patientReference: String? = null, val encounterId: String? = null)
