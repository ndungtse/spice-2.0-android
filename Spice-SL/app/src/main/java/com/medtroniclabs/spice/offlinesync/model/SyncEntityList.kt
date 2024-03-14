package com.medtroniclabs.spice.offlinesync.model

data class SyncEntityList(
    val type: String?,
    val requestId: String?,
    val fhirId: String?,
    val status: String?,
    val errorMessage: String?,
    val referenceId: String?
)
