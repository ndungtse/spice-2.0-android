package org.medtroniclabs.uhis.data

data class MedicationSearchRequest(val searchTerm: String, val skip: Int = 0, val limit: Int = 0)

data class MedicationGroupSearchRequest(val groupName: String)
