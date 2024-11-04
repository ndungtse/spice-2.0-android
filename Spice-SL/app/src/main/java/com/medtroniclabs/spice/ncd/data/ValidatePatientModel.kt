package com.medtroniclabs.spice.ncd.data

data class ValidatePatientModel(
    val identityType: String? = null,
    val identityValue: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val country: Country? = null
)

data class Country(
    val id: Long? = null
)
