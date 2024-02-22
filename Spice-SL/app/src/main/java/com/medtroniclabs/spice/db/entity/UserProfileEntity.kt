package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserProfileEntity")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val profileData: String
)


data class UserProfile(
    val id: Long,
    val firstName: String?,
    val roles: List<RoleEntity>,
    val middleName: String?,
    val lastName: String?,
    val gender: String?,
    val phoneNumber: String?,
    val username: String,
    val countryCode: String?,
    val country: CountryEntity?,
    val organizations: List<OrganizationEntity>? = null,
    val tenantId: Int,
    val suiteAccess: List<String?>?,
    val supervisor: String?
)

data class RoleEntity(
    val id: Long,
    val name: String,
    val level: Long?,
    val authority: String
)

data class CountryEntity(
    val id: Long,
    val name: String,
    val phoneNumberCode: String?,
    val unitMeasurement: Long?,
    val regionCode: String?,
    val tenantId: Long
)

data class OrganizationEntity(
    val id: Long,
    val formDataId: Long?,
    val name: String?,
    val sequence: String?,
    val parentOrganizationId: Long?
)