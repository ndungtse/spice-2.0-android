package com.medtroniclabs.spice.ui.boarding.repo

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.EncryptionUtil
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.ClinicalWorkflow
import com.medtroniclabs.spice.data.ErrorResponse
import com.medtroniclabs.spice.data.FormMetaRequest
import com.medtroniclabs.spice.data.FormRequest
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.data.MenuDetail
import com.medtroniclabs.spice.data.Village
import com.medtroniclabs.spice.db.entity.ClinicalWorkflowEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.Menu
import com.medtroniclabs.spice.db.entity.MenuAdapterModel
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.UserProfile
import com.medtroniclabs.spice.db.entity.UserProfileEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.MenuConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import java.lang.reflect.Type
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
        clinicalWorkflowIds: MutableList<Int>,
        meta: MutableList<String>
    ) {
        try {
            metaDataCompleteLiveData.postLoading()
            withContext(Dispatchers.IO) {
                val res = async {
                    apiHelper.getMetaDataInformation()
                }
                val response = res.await()

                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.entity
                    with(roomHelper) {
                        data?.nearestHealthFacilities?.let { list ->
                            deleteAllHealthFacility()
                            val defaultId = data.defaultHealthFacility.id
                            list.forEach { healthFacility ->
                                val baseType: Type =
                                    object : TypeToken<ArrayList<ClinicalWorkflow>>() {}.type
                                val resultString =
                                    Gson().toJson(healthFacility.clinicalWorkflows, baseType)
                                val baseTypeVillage: Type =
                                    object : TypeToken<ArrayList<Village>>() {}.type
                                val villageResultString =
                                    Gson().toJson(healthFacility.linkedVillages, baseTypeVillage)
                                saveHealthFacility(
                                    HealthFacilityEntity(
                                        id = healthFacility.id,
                                        name = healthFacility.name,
                                        type = healthFacility.type,
                                        districtId = healthFacility.districtId,
                                        chiefdomId = healthFacility.chiefdomId,
                                        latitude = healthFacility.latitude,
                                        longitude = healthFacility.longitude,
                                        postalCode = healthFacility.postalCode,
                                        language = healthFacility.language,
                                        tenantId = healthFacility.tenantId,
                                        clinicalWorkflows = resultString,
                                        isDefault = healthFacility.id == defaultId,
                                        linkedVillages = villageResultString
                                    )
                                )
                            }
                        }

                        data?.villages?.let { villages ->
                            deleteAllVillages()
                            saveVillage(villages)
                        }

                        data?.userProfile?.let { userProfile ->
                            deleteAllUserProfileDetails()
                            saveUserProfileDetails(
                                UserProfileEntity(
                                    id = 0,
                                    profileData = Gson().toJson(userProfile)
                                )
                            )
                        }

                        data?.menu?.let { menu ->
                            deleteAllMenus()
                            val baseType: Type =
                                object : TypeToken<ArrayList<MenuDetail>>() {}.type
                            val resultString =
                                Gson().toJson(menu.menus, baseType)
                            saveMenus(
                                MenuEntity(
                                    id = 0,
                                    roleName = menu.roleName,
                                    menus = resultString,
                                    active = menu.active,
                                    deleted = menu.deleted,
                                    menuType = MenuConstants.DASHBOARD
                                )
                            )
                            meta.addAll(menu.meta)
                            data?.clinicalWorkflows?.let { clinicalWorkflows ->
                                val list = ArrayList<MenuDetail>()
                                clinicalWorkflows.forEach {
                                    list.add(MenuDetail(name = it.name, order = it.order))
                                }

                                val baseType: Type =
                                    object : TypeToken<ArrayList<MenuDetail>>() {}.type
                                val resultString =
                                    Gson().toJson(list, baseType)
                                saveMenus(
                                    MenuEntity(
                                        id = 0,
                                        roleName = menu.roleName,
                                        menus = resultString,
                                        active = menu.active,
                                        deleted = menu.deleted,
                                        menuType = MenuConstants.TOOL
                                    )
                                )
                            }
                        }

                        data?.clinicalWorkflows?.let { clinicalWorkflows ->
                            clinicalWorkflows.forEach { clinicalWorkflow ->
                                clinicalWorkflowIds.add(clinicalWorkflow.id.toInt())
                                saveClinicalWorkflow(
                                    ClinicalWorkflowEntity(
                                        id = clinicalWorkflow.id,
                                        name = clinicalWorkflow.name,
                                        moduleType = clinicalWorkflow.moduleType,
                                        workflowName = clinicalWorkflow.workflowName,
                                        countryId = clinicalWorkflow.countryId,
                                        active = clinicalWorkflow.active ?: false,
                                        deleted = clinicalWorkflow.deleted ?: false,
                                        order = clinicalWorkflow.order
                                    )
                                )
                            }
                        }

                    }
                    val resForm = async {
                        apiHelper.getForms(FormRequest(clinicalWorkflowIds))
                    }
                    val responseForm = resForm.await()
                    if (responseForm.isSuccessful && responseForm.body()?.status == true) {
                        responseForm.body()?.entity?.let { forms ->
                            roomHelper.deleteAllForms()
                            forms.forEach { form ->
                                roomHelper.saveForm(form)
                            }
                        }
                    } else {
                        metaDataCompleteLiveData.postError()
                    }
                    val resMetadata = async {
                        apiHelper.getFormMetadata(FormMetaRequest(meta))
                    }
                    val responseFormMetadata = resMetadata.await()
                    if (responseFormMetadata.isSuccessful && responseFormMetadata.body()?.status == true) {
                        responseFormMetadata.body()?.entity?.let { forms ->
                            roomHelper.deleteAllSymptoms()
                            forms.symptoms.forEach { symptoms ->
                                roomHelper.insertSymptoms(symptoms)
                            }
                        }
                    } else {
                        metaDataCompleteLiveData.postError()
                    }
                    saveUserIsLogin()
                    metaDataCompleteLiveData.postSuccess()
                } else {
                    metaDataCompleteLiveData.postError()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            metaDataCompleteLiveData.postError()
        }
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
        menuListLiveData: MutableLiveData<Resource<List<MenuAdapterModel>>>,
        dashboard: String
    ) {
        try {
            menuListLiveData.postLoading()
            val data = roomHelper.getMenus()
            val adapterData = convertMenuEntityToAdapterModel(data, dashboard)
            menuListLiveData.postSuccess(adapterData)
        } catch (e: Exception) {
            menuListLiveData.postError()
        }
    }

    private fun convertMenuEntityToAdapterModel(
        list: List<MenuEntity>,
        dashboard: String
    ): List<MenuAdapterModel> {
        return list
            .filter { it.menuType == dashboard }
            .flatMap { data ->
                val menuListType = object : TypeToken<List<Menu>>() {}.type
                val menus: List<Menu> = Gson().fromJson(data.menus, menuListType)
                menus.map {
                    MenuAdapterModel(
                        name = it.name,
                        role = data.roleName,
                        menuId = it.name,
                        displayOrder = it.id
                    )
                }
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


    suspend fun getAllVillagesName(villageListResponse: MutableLiveData<List<VillageEntity>>) {
        try {
            val response = roomHelper.getAllVillageName()
            villageListResponse.postValue(response)
        } catch (e: Exception) {
            // occurred error response
        }
    }

    suspend fun getDefaultHealthFacility(defaultHealthFacilityLiveData: MutableLiveData<HealthFacilityEntity?>) {
        try {
            val response = roomHelper.getDefaultHealthFacility()
            defaultHealthFacilityLiveData.postValue(response)
        } catch (e: Exception) {
            // occurred error response
        }
    }
}