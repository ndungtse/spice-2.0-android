package com.medtroniclabs.spice.ui.boarding.repo

import com.google.gson.Gson
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.RoleConstant.PROVIDER
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.ClinicalWorkflow
import com.medtroniclabs.spice.data.FormData
import com.medtroniclabs.spice.data.FormMetaRequest
import com.medtroniclabs.spice.data.FormRequest
import com.medtroniclabs.spice.data.HealthFacility
import com.medtroniclabs.spice.data.MenuDetail
import com.medtroniclabs.spice.data.UserProfile
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowConditionEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntityWithSubmodule
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MetaRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    suspend fun getMetaDataInformation(
        workflowNames: MutableList<Long>,
        meta: MutableList<String>
    ): Resource<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val response = async { apiHelper.getMetaDataInformation() }.await()

                if (response.isSuccessful && response.body()?.status == true) {
                    with(roomHelper) {
                        response.body()?.entity?.apply {
                            saveHealthFacilityInDb(
                                nearestHealthFacilities,
                                defaultHealthFacility.id
                            )
                            SecuredPreference.putString(
                                SecuredPreference.EnvironmentKey.DEFAULT_SITE_ID.name,
                                defaultHealthFacility.fhirId
                            )
                            deleteAllVillages()
                            saveVillage(villages)
                            deleteAllFrequencyList()
                            frequency?.let {
                                saveFrequencyList(it)
                            }
                            saveFhirId(userProfile.fhirId, defaultHealthFacility.fhirId)
                            saveUserProfileDetailsInDb(userProfile)
                            if (CommonUtils.isRolePresent()) {
                                saveClinicalWorkflowsForProvider(defaultHealthFacility.clinicalWorkflows)
                            } else {
                                saveMenusInDb(menu.menus, menu.roleName)
                                menu.meta?.let { meta.addAll(it) }
                            }
                            workflowNames.addAll(clinicalIds)
                        }

//                        if (CommonUtils.isChw()) {
                            val formsResponse =
                                async { apiHelper.getForms(FormRequest(workflowNames)) }.await()
                            if (formsResponse.isSuccessful && formsResponse.body()?.status == true) {
                                if (formsResponse.body()?.entity == null) {
                                    return@with Resource(state = ResourceState.ERROR)
                                }
                                formsResponse.body()?.entity?.apply {
                                    if (formData == null && clinicalTools == null) {
                                        return@with Resource(state = ResourceState.ERROR)
                                    }
                                    formData?.let {
                                        saveFormsInDb(it)
                                    }
                                    clinicalTools?.let {
                                        saveClinicalWorkflowsInDb(it)
                                    }
                                }
                            } else {
                                return@with Resource(state = ResourceState.ERROR)
                            }
//                        }
                        if (meta.isNotEmpty()) {
                            val metadataResponse =
                                async { apiHelper.getFormMetadata(FormMetaRequest(meta)) }.await()
                            if (metadataResponse.isSuccessful && metadataResponse.body()?.status == true) {
                                if (metadataResponse.body()?.entity == null) {
                                    return@with Resource(state = ResourceState.ERROR)
                                }
                                metadataResponse.body()?.entity?.symptoms?.let {
                                    roomHelper.deleteAllSymptoms()
                                    roomHelper.insertSymptoms(it)
                                }
                            } else {
                                return@with Resource(state = ResourceState.ERROR)
                            }
                        } else {
                            roomHelper.deleteAllSymptoms()
                        }
                        Resource(state = ResourceState.SUCCESS)
                    }
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }
    }

    private suspend fun saveClinicalWorkflowsForProvider(clinicalWorkflows: List<ClinicalWorkflow>) {
        val workflowList = clinicalWorkflows.filter { workflow ->
            workflow.conditions?.any { condition ->
                condition.moduleType == MedicalReviewTypeEnums.medicalReview.name.lowercase()
            } ?: false
        }
        val menuList = ArrayList<MenuDetail>()
        workflowList.forEach {
            menuList.add(
                MenuDetail(
                    name = it.workflowName,
                    order = it.displayOrder,
                    workflowName = it.workflowName
                )
            )
        }
        saveMenusInDb(menuList, PROVIDER)
    }

    private suspend fun saveHealthFacilityInDb(list: List<HealthFacility>, defaultId: Long) {
        roomHelper.deleteAllHealthFacility()
        list.forEach { healthFacility ->
            healthFacility.district?.let {
                HealthFacilityEntity(
                    id = healthFacility.id,
                    name = healthFacility.name,
                    districtId = it.id,
                    chiefdomId = healthFacility.chiefdomId,
                    tenantId = healthFacility.tenantId,
                    fhirId = healthFacility.fhirId,
                    isDefault = healthFacility.id == defaultId,
                )
            }?.let {
                roomHelper.saveHealthFacility(
                    it
                )
            }
        }
    }

    private fun saveFhirId(userId: String?, organizationId: String?) {
        userId?.let {
            SecuredPreference.putString(SecuredPreference.EnvironmentKey.USER_FHIR_ID.name, it)
        }

        organizationId?.let {
            SecuredPreference.putString(
                SecuredPreference.EnvironmentKey.ORGANIZATION_FHIR_ID.name,
                it
            )
        }
    }

    private suspend fun saveClinicalWorkflowsInDb(clinicalTools: List<ClinicalWorkflow>) {
        val clinicalWorkFlowList = mutableListOf<ClinicalWorkflowEntity>()
        val clinicalWorkFlowConditions = mutableListOf<ClinicalWorkflowConditionEntity>()
        clinicalTools.forEach { clinicalWorkflow ->
            clinicalWorkFlowList.add(
                ClinicalWorkflowEntity(
                    id = clinicalWorkflow.id,
                    name = clinicalWorkflow.name,
                    moduleType = clinicalWorkflow.moduleType,
                    workflowName = clinicalWorkflow.workflowName,
                    countryId = clinicalWorkflow.countryId,
                    displayOrder = clinicalWorkflow.displayOrder,
                )
            )
            clinicalWorkFlowConditions.addAll(clinicalWorkflow.conditions?.map { condition ->
                ClinicalWorkflowConditionEntity(
                    id = 0,
                    gender = condition.gender,
                    maxAge = condition.maxAge,
                    minAge = condition.minAge,
                    clinicalWorkflowId = clinicalWorkflow.id,
                    subModule = condition.subModule,
                    moduleType = condition.moduleType
                )

            }?.toList() ?: listOf())
        }
        roomHelper.deleteAllClinicalWorkflow()
        roomHelper.deleteClinicalWorkflowConditions()
        roomHelper.saveClinicalWorkflows(clinicalWorkFlowList)
        roomHelper.insertClinicalWorkflowConditions(clinicalWorkFlowConditions)
    }

    private suspend fun saveUserProfileDetailsInDb(userProfile: UserProfile) {
        roomHelper.deleteAllUserProfileDetails()
        roomHelper.saveUserProfileDetails(
            UserProfileEntity(
                id = 0,
                profileData = Gson().toJson(userProfile)
            )
        )
    }

    private suspend fun saveMenusInDb(menus: ArrayList<MenuDetail>, roleName: String) {
        roomHelper.deleteAllMenus()
        menus.forEach { menu ->
            roomHelper.saveMenus(
                MenuEntity(
                    id = 0,
                    roleName = roleName,
                    name = menu.name,
                    displayOrder = menu.order,
                    menuId = menu.workflowName ?: menu.name
                )
            )
        }
    }

    private suspend fun saveFormsInDb(formData: List<FormData>) {
        roomHelper.deleteAllForms()
        roomHelper.saveForms(formData.map { data ->
            FormEntity(
                id = data.id,
                formInput = data.formInput,
                formType = data.formType,
                workflowName = data.workflowName,
                clinicalWorkflowId = data.clinicalWorkflowId
            )
        })
    }

    private fun saveUserIsLogin() {
        SecuredPreference.putBoolean(
            SecuredPreference.EnvironmentKey.ISLOGGEDIN.name,
            true
        )
        SecuredPreference.putBoolean(
            SecuredPreference.EnvironmentKey.ISMETALOADED.name,
            true
        )
    }


    suspend fun getMenuForClinicalWorkflows(selectedHouseholdMemberID: Long): Resource<List<MenuEntity>> {
        return try {
            if (selectedHouseholdMemberID != -1L) {
                val memberData = roomHelper.getDobAndGenderById(selectedHouseholdMemberID)
                val (months, weeks) = DateUtils.dateToMonthsAndWeeks(memberData.dateOfBirth) ?: Pair(0, 0)
                val list = if (months == 15 && weeks == 0) {
                    roomHelper.getClinicalWorkflowId(memberData.gender, months.minus(1))
                } else {
                    roomHelper.getClinicalWorkflowId(memberData.gender, months)
                }

                Resource(
                    state = ResourceState.SUCCESS,
                    data = convertorClinicalWorkflowsToMenuEntity(list)
                )
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    private fun convertorClinicalWorkflowsToMenuEntity(clinicalWorkflows: List<ClinicalWorkflowEntityWithSubmodule>): List<MenuEntity> {
        return clinicalWorkflows.sortedBy { it.displayOrder }.map { clinicalWorkflow ->
            MenuEntity(
                id = clinicalWorkflow.id,
                menuId = clinicalWorkflow.workflowName,
                name = clinicalWorkflow.name,
                displayOrder = clinicalWorkflow.displayOrder ?: 0,
                subModule = clinicalWorkflow.subModule
            )
        }
    }

    suspend fun getMenu(): Resource<List<MenuEntity>> {
        return try {
            val data = roomHelper.getMenus()
            Resource(state = ResourceState.SUCCESS, data = data)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getUserProfile(): Resource<UserProfile> {
        return try {
            val data = roomHelper.getUserProfile()
            val userProfile: UserProfile =
                Gson().fromJson(data.profileData, UserProfile::class.java)
            Resource(state = ResourceState.SUCCESS, data = userProfile)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getAllVillagesName(): Resource<List<VillageEntity>> {
        return try {
            val response = roomHelper.getAllVillageEntity()
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getDefaultHealthFacility(): Resource<HealthFacilityEntity> {
        return try {
            val response = roomHelper.getDefaultHealthFacility()
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getAllVillageIds(): List<Long> {
        return roomHelper.getAllVillageIds()
    }
}