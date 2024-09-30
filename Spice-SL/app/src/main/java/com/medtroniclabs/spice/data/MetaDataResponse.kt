package com.medtroniclabs.spice.data

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import com.medtroniclabs.spice.db.entity.ChiefDomEntity
import com.medtroniclabs.spice.db.entity.FrequencyEntity
import com.medtroniclabs.spice.db.entity.MedicalComplianceEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.entity.DistrictEntity
import com.medtroniclabs.spice.db.entity.RiskClassificationModel
import com.medtroniclabs.spice.db.entity.VillageEntity

data class MetaDataResponse(
    val nearestHealthFacilities: List<HealthFacility>,
    val villages: List<VillageEntity>,
    val defaultHealthFacility: HealthFacility,
    val menu: Menu,
    val userProfile: UserProfile,
    val clinicalIds: ArrayList<Long>,
    val frequency: List<FrequencyEntity> ? = null,
    val userHealthFacilities: List<HealthFacility>? = null,
    val identityTypes: ArrayList<IdentityType>? = null,
    val districts: ArrayList<DistrictEntity>? = null,
    val chiefdoms: ArrayList<ChiefDomEntity>? = null,
    val programs: ArrayList<ProgramEntity>? = null,
    val cultures: ArrayList<CulturesEntity>? = null,
    val nonNcdWorkflowEnabled: Boolean = false,
    val remainingAttemptsCount: Int? = null,
    val consentForm: ConsentFormResponse ? = null

)

data class HealthFacility(
    val id: Long,
    val name: String,
    val type: String?,
    val district: District,
    val chiefdomId: Long,
    val latitude: String?,
    val longitude: String?,
    val postalCode: String?,
    val language: String?,
    val tenantId: Long,
    val fhirId: String?,
    val clinicalWorkflows: ArrayList<ClinicalWorkflow>,
    val linkedVillages: ArrayList<Village>
)

data class District(
    val id: Long,
    val name: String?,
    val code: String?
)

data class ClinicalWorkflow(
    val id: Long,
    val name: String,
    val moduleType: String,
    val workflowName: String,
    val countryId: Long,
    val active: Boolean? = false,
    val deleted: Boolean? = false,
    val displayOrder: Int,
    val conditions: ArrayList<ClinicalWorkflowCondition>? = null
)

data class ClinicalWorkflowCondition(
    val gender: String? = null,
    val maxAge: Int? = null,
    val minAge: Int? = null,
    val subModule: String? = null,
    val groupName: String? = null,
    val moduleType: String,
    val category: String
)

data class Village(
    val id: Long,
    val name: String,
    val code: String? = null,
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
    val meta: List<String>? = null
)

data class MenuDetail(
    val name: String,
    val order: Int,
    val workflowName: String? = null
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
    val fhirId: String?,
    val organizations: List<Organization>? = null,
    val tenantId: Long,
    val suiteAccess: List<String>? = null,
    val supervisor: String?
)

data class Role(
    val id: Long,
    val name: String,
    val displayName: String?,
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
    val parentOrganizationId: Long?
)

data class FormRequest(
    val nonNcdWorkflowEnabled: Boolean? = null,
    val workflowIds: List<Long>
)

data class FormResponse(
    val formData: List<FormData>? = null,
    val clinicalTools: List<ClinicalWorkflow>? = null,
    val id: Long,
    val enrollment: NcdFormData? = null,
    val screening: NcdFormData? = null,
    val assessment: NcdFormData? = null,
    val customizedWorkflow: List<NcdCustomizedWorkflow>? = null,
    val modelQuestions: List<ModelQuestion>? = null
)

data class NcdFormData(
    val id: Long,
    val inputForm: String,
    val consentForm: String
)

data class NcdCustomizedWorkflow(
    val viewScreens: List<String>,
    val formInput: String,
    val id: Long
)

data class ModelQuestion(
    val type: String,
    val questions: String?
)

data class FormData(
    val id: Long,
    val formInput: String,
    val formType: String,
    val workflowName: String? = null,
    val clinicalWorkflowId: Long? = null
)

data class FormMetaRequest(
    val metaNames: List<String>
)

data class UserSymptomsEntity(
    val symptoms: ArrayList<SignsAndSymptomsEntity>,
    var medicalCompliances: ArrayList<MedicalComplianceEntity>? = null,
    val cvdRiskAlgorithms: RiskFactorResponse? = null
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

data class ConsentFormResponse(val household: String?)

data class RiskFactorResponse(
    @SerializedName("non_lab")
    var nonLab: ArrayList<RiskClassificationModel>?
)