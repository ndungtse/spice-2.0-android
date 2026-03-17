package org.medtroniclabs.uhis.data

data class BirthHistoryRequest(
    var memberId: String? = null,
    var motherPatientId: String? = null,
)
