package com.medtroniclabs.spice.data

data class MedicationSearchRequest(val searchTerm: String, val skip: Int = 0, val limit: Int = 0)