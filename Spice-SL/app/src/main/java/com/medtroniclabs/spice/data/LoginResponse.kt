package com.medtroniclabs.spice.data

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val username: String?,
    val isActive: Boolean,
    val roles: List<UserRole>,
    val id: Long,
    val authorization: String?,
    var deviceInfoId: Long?,
    val countryCode: String?,
    val country: CountryModel,
    val currentDate: Long,
    var timezone: TimeZoneModel? = null,
    var tenantId: Long,
    var cultureId: Long? = null,
    val organizations: ArrayList<OrganizationModel>? = null,
    val isSuperUser: Boolean,
    val suiteAccess: List<String>,
    val client: String,
)

data class TimeZoneModel(
    val id: Long,
    val offset: String? = null,
    val description: String
)

data class CountryModel(
    val id: Long,
    val name: String,
    val countryCode: String? = null,
    val phoneNumberCode: String? = null,
    val unitMeasurement:String? = null,
    val tenantId: Long? = null
)

data class OrganizationModel(
    val id: Long,
    val name: String
)

data class UserRole(
    val id: Int,
    val name: String,
    val level: Int,
    val authority: String
)