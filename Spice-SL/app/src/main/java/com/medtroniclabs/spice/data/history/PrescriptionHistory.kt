package com.medtroniclabs.spice.data.history

import com.medtroniclabs.spice.model.ReferredDate

data class HistoryEntity(
    val encounterId: String? = null,
    val dateOfReview: String? = null,
    val patientReference: String? = null,
    val history: List<ReferredDate>? = null,
    val prescriptions: List<Prescription>? = null,
    val investigations: List<Investigation>? = null
)

data class Prescription(
    val prescribedDays: Int? = null,
    val medicationName: String? = null,
    val medicationId: String? = null,
    val frequency: Int? = null,
    var frequencyName: String? = null,
    val prescribedSince: String? = null,
    val endDate: String? = null,
    val prescriptionId: String? = null,
    val isActive: Boolean? = null
)

data class Investigation(
    val id: String,
    val testName: String,
    val patientId: String
)
