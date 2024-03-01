package com.medtroniclabs.spice.data

import androidx.room.ColumnInfo
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.VillageEntity

data class MetaDataResponse(
    val nearestHealthFacilities: List<HealthFacility>,
    val villages: List<VillageEntity>,
    val defaultHealthFacility: HealthFacility,
    val menu: Menu,
    val userProfile: UserProfile,
    val clinicalWorkflows: ArrayList<ClinicalWorkflow>
)

data class HealthFacility(
    val id: Long,
    val name: String,
    val type: String?,
    val districtId: Long,
    val chiefdomId: Long,
    val latitude: String?,
    val longitude: String?,
    val postalCode: String?,
    val language: String?,
    val tenantId: Long,
    val clinicalWorkflows: ArrayList<ClinicalWorkflow>,
    val linkedVillages: ArrayList<Village>
)

data class ClinicalWorkflow(
    val id: Long,
    val name: String,
    val moduleType: String,
    val workflowName: String,
    val countryId: Long,
    val active: Boolean? = false,
    val deleted: Boolean? = false,
    val order: Int
)

data class Village(
    val id: Long,
    val name: String,
    val code: String,
    val chiefdomId: Long,
    val countryId: Long,
    val districtId: Long
)

data class Menu(
    val id: Long,
    val roleName: String,
    val menus: ArrayList<MenuDetail>,
    val active: Boolean? = false,
    val deleted: Boolean? = false,
    val meta: List<String>
)

data class MenuDetail(
    val name: String,
    val order: Int
)

data class UserProfile(
    val id: Long,
    val firstName: String?,
    val roles: List<Role>,
    val middleName: String?,
    val lastName: String?,
    val gender: String?,
    val phoneNumber: String?,
    val username: String,
    val countryCode: String?,
    val country: Country?,
    val organizations: List<Organization>? = null,
    val tenantId: Long,
    val suiteAccess: List<String>? = null,
    val supervisor: String?
)

data class Role(
    val id: Long,
    val name: String,
    val level: Int,
    val authority: String
)

data class Country(
    val id: Long,
    val name: String,
    val phoneNumberCode: String,
    val unitMeasurement: String?,
    val regionCode: String,
    val tenantId: Long
)

data class Organization(
    val id: Long,
    val formDataId: Long,
    val name: String,
    val sequence: String?,
    val parentOrganizationId: String?
)

data class FormRequest(
    val ids: List<Int>
)

data class FormMetaRequest(
    val metaNames: List<String>
)
data class UserSymptomsEntity(
    val symptoms: ArrayList<SignsAndSymptomsEntity>
)
data class VillageInfo(
    val chiefdomId: Long,
    val code: String
)

data class LastCreatedAtAndPatientId(
    @ColumnInfo(name = "lastCreatedAt")
    val lastCreatedAt: Long,
    @ColumnInfo(name = "lastPatientId")
    val lastPatientId: String?
)