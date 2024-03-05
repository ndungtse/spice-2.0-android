package com.medtroniclabs.spice.ui.boarding.repo

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
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
import com.medtroniclabs.spice.db.entity.FormEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
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

    suspend fun doLogin(
        username: String,
        password: String,
        loginResponseLiveDta: MutableLiveData<Resource<LoginResponse>>
    ) {
        try {
            loginResponseLiveDta.postLoading()
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
                loginResponseLiveDta.postSuccess(response.body())
            } else {
                loginResponseLiveDta.postError(getErrorMessage(response.errorBody()))
            }
        } catch (e: Exception) {
            loginResponseLiveDta.postError()
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
            SecuredPreference.EnvironmentKey.USERNAME.name,
            userName
        )
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
        metaDataCompleteLiveData: MutableLiveData<Resource<Boolean>>,
        workflowNames: MutableList<String>,
        meta: MutableList<String>
    ) {
        try {
            metaDataCompleteLiveData.postLoading()
            withContext(Dispatchers.IO) {
                val response = async { apiHelper.getMetaDataInformation() }.await()

                if (response.isSuccessful && response.body()?.status == true) {
                    with(roomHelper) {
                        response.body()?.entity?.apply {
                            saveHealthFacilityInDb(
                                nearestHealthFacilities,
                                defaultHealthFacility.id
                            )
                            saveVillage(villages)
                            saveUserProfileDetailsInDb(userProfile)
                            saveMenusInDb(menu.menus, menu.roleName)
                            meta.addAll(menu.meta)
                            workflowNames.addAll(clinicalNames)
                        }

                        val formsResponse =
                            async { apiHelper.getForms(FormRequest(workflowNames)) }.await()
                        if (formsResponse.isSuccessful && formsResponse.body()?.status == true) {
                            formsResponse.body()?.entity?.apply {
                                saveFormsInDb(formData)
                                saveClinicalWorkflowsInDb(clinicalTools)
                            } ?: metaDataCompleteLiveData.postError()
                        }

                        val metadataResponse =
                            async { apiHelper.getFormMetadata(FormMetaRequest(meta)) }.await()
                        if (metadataResponse.isSuccessful && metadataResponse.body()?.status == true) {
                            metadataResponse.body()?.entity?.symptoms?.let {
                                roomHelper.deleteAllSymptoms()
                                roomHelper.insertSymptoms(it)
                            } ?: metaDataCompleteLiveData.postError()
                        }
                        saveUserIsLogin()
                        metaDataCompleteLiveData.postSuccess()
                    }
                } else {
                    metaDataCompleteLiveData.postError()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            metaDataCompleteLiveData.postError()
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
                    clinicalWorkflowId = clinicalWorkflow.id
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
            roomHelper.saveHealthFacility(
                HealthFacilityEntity(
                    id = healthFacility.id,
                    name = healthFacility.name,
                    districtId = healthFacility.districtId,
                    chiefdomId = healthFacility.chiefdomId,
                    tenantId = healthFacility.tenantId,
                    isDefault = healthFacility.id == defaultId,
                )
            )
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
                formType = formData.formType
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

    suspend fun getMenu(
        menuListLiveData: MutableLiveData<Resource<List<MenuEntity>>>,
    ) {
        try {
            menuListLiveData.postLoading()
            val data = roomHelper.getMenus()
            menuListLiveData.postSuccess(data)
        } catch (e: Exception) {
            menuListLiveData.postError()
        }
    }

    suspend fun getMenuForClinicalWorkflows(
        menuListLiveData: MutableLiveData<Resource<List<MenuEntity>>>,
    ) {
        try {
            menuListLiveData.postLoading()
            val data = roomHelper.getMenuForClinicalWorkflows()
            menuListLiveData.postSuccess(convertorClinicalWorkflowsToMenuEntity(data))
        } catch (e: Exception) {
            menuListLiveData.postError()
        }
    }

    private fun convertorClinicalWorkflowsToMenuEntity(clinicalWorkflows: List<ClinicalWorkflowEntity>): List<MenuEntity> {
        return clinicalWorkflows.map { clinicalWorkflow ->
            MenuEntity(
                id = clinicalWorkflow.id,
                menuId = clinicalWorkflow.name,
                name = clinicalWorkflow.workflowName,
                displayOrder = clinicalWorkflow.displayOrder?:0,
            )
        }
    }

    suspend fun getUserProfile(userProfileLiveData: MutableLiveData<Resource<UserProfile>>) {
        try {
            userProfileLiveData.postLoading()
            val data = roomHelper.getUserProfile()
            val userProfile: UserProfile =
                Gson().fromJson(data.profileData, UserProfile::class.java)
            userProfileLiveData.postSuccess(userProfile)
        } catch (e: Exception) {
            userProfileLiveData.postError()
        }
    }


    suspend fun getAllVillagesName(villageListResponse: MutableLiveData<Resource<List<VillageEntity>>>) {
        try {
            villageListResponse.postLoading()
            val response = roomHelper.getAllVillageName()
            villageListResponse.postSuccess(response)
        } catch (e: Exception) {
            // occurred error response
            villageListResponse.postError()
        }
    }

    suspend fun getDefaultHealthFacility(defaultHealthFacilityLiveData: MutableLiveData<Resource<HealthFacilityEntity?>>) {
        try {
            defaultHealthFacilityLiveData.postLoading()
            val response = roomHelper.getDefaultHealthFacility()
            defaultHealthFacilityLiveData.postSuccess(response)
        } catch (e: Exception) {
            // occurred error response
            defaultHealthFacilityLiveData.postError()
        }
    }
}