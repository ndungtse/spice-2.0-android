package com.medtroniclabs.spice.ui.boarding.repo

import com.google.gson.Gson
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.EncryptionUtil
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.ClinicalWorkflow
import com.medtroniclabs.spice.data.ErrorResponse
import com.medtroniclabs.spice.data.FormData
import com.medtroniclabs.spice.data.FormMetaRequest
import com.medtroniclabs.spice.data.FormRequest
import com.medtroniclabs.spice.data.HealthFacility
import com.medtroniclabs.spice.data.LoginResponse
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    suspend fun doLogin(username: String, password: String): Resource<LoginResponse> {
        return try {
            val securePassword = EncryptionUtil.getSecurePassword(password)
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
            builder.addFormDataPart(DefinedParams.Username, username)
            builder.addFormDataPart(DefinedParams.Password, securePassword)
            val response = apiHelper.doLogin(builder.build())
            if (response.isSuccessful) {
                val headers = response.headers().toMultimap()
                val loginResponseModel = response.body()
                saveTokenInformation(headers)
                loginResponseModel?.let {
                    SecuredPreference.putUserDetails(it)
                    saveUserNameAndPassword(username, securePassword)
                }
                Resource(state = ResourceState.SUCCESS, data = response.body())
            } else {
                Resource(
                    state = ResourceState.ERROR,
                    message = getErrorMessage(response.errorBody())
                )
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    private fun getErrorMessage(errorBody: ResponseBody?): String? {
        if (errorBody == null)
            return null
        return try {
            val errorResponse = Gson().fromJson(errorBody.string(), ErrorResponse::class.java)
            return errorResponse.message
        } catch (e: Exception) {
            null
        }
    }

    private fun saveUserNameAndPassword(userName: String, password: String) {
        SecuredPreference.putString(
            SecuredPreference.EnvironmentKey.PASSWORD.name,
            password
        )
        SecuredPreference.putBoolean(
            SecuredPreference.EnvironmentKey.ISLOGGEDIN.name,
            true
        )
    }

    private fun saveTokenInformation(headers: Map<String, List<String>>) {
        if (headers.containsKey(DefinedParams.Authorization)
            && (headers[DefinedParams.Authorization]?.size
                ?: 0) > 0
        ) {
            headers[DefinedParams.Authorization]?.get(0)?.let { token ->
                SecuredPreference.putString(
                    SecuredPreference.EnvironmentKey.TOKEN.name,
                    token
                )
            }
        }
    }

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
                            saveFhirId(userProfile.fhirId, defaultHealthFacility.fhirId)
                            saveUserProfileDetailsInDb(userProfile)
                            saveMenusInDb(menu.menus, menu.roleName)
                            menu.meta?.let { meta.addAll(it) }
                            workflowNames.addAll(clinicalIds)
                        }

                        if (CommonUtils.isChw()) {
                            val formsResponse =
                                async { apiHelper.getForms(FormRequest(workflowNames)) }.await()
                            if (formsResponse.isSuccessful && formsResponse.body()?.status == true) {
                                if (formsResponse.body()?.entity == null) {
                                    return@with Resource(state = ResourceState.ERROR)
                                }
                                formsResponse.body()?.entity?.apply {
                                    saveFormsInDb(formData)
                                    saveClinicalWorkflowsInDb(clinicalTools)
                                }
                            } else {
                                return@with Resource(state = ResourceState.ERROR)
                            }
                        }
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
                        }
                        saveUserIsLogin()
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
                    menuId = menu.name
                )
            )
        }
    }

    private suspend fun saveFormsInDb(formData: List<FormData>) {
        roomHelper.deleteAllForms()
        roomHelper.saveForms(formData.map { formData ->
            FormEntity(
                id = formData.id,
                formInput = formData.formInput,
                formType = formData.formType,
                workflowName = formData.workflowName,
                clinicalWorkflowId = formData.clinicalWorkflowId
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
                val list = roomHelper.getClinicalWorkflowId(
                    memberData.gender,
                    DateUtils.dateToMonths(memberData.dateOfBirth) ?: 0
                )
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

    suspend fun getMenu(): Resource<List<MenuEntity>> {
        return try {
            val data = roomHelper.getMenus()
            Resource(state = ResourceState.SUCCESS, data = data)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }
}