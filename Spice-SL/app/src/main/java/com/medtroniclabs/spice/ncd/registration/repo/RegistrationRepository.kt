package com.medtroniclabs.spice.ncd.registration.repo

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.CountryModel
import com.medtroniclabs.spice.data.ErrorResponse
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.model.RegistrationResponse
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.ncd.data.ValidatePatientModel
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import okhttp3.RequestBody
import okhttp3.ResponseBody
import javax.inject.Inject

class RegistrationRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {
    fun fetchConsentForm(formType: String): LiveData<String> {
        return roomHelper.getConsent(formType)
    }

    suspend fun getFormData(
        formType: String
    ): Resource<FormResponse> {
        return try {
            val response = roomHelper.getFormData(formType)
            val formFieldsType = object : TypeToken<FormResponse>() {}.type
            val formFields: FormResponse = Gson().fromJson(response, formFieldsType)
            Resource(state = ResourceState.SUCCESS, data = formFields)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    fun getCountries(
        tag: String
    ): Resource<LocalSpinnerResponse> {
        return try {
            val countryList = ArrayList<CountryModel>()
            SecuredPreference.getUserDetails()?.country?.let { country ->
                countryList.add(country)
                Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, countryList))
            } ?: run {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getCounties(
        tag: String,
        selectedParent: Long
    ): Resource<LocalSpinnerResponse> {
        return try {
            val response = roomHelper.getDistricts(selectedParent)
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getSubCounties(
        tag: String,
        selectedParent: Long
    ): Resource<LocalSpinnerResponse> {
        return try {
            val response = roomHelper.getChiefDoms(selectedParent)
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getAllVillages(
        tag: String,
        selectedParent: Long
    ): Resource<LocalSpinnerResponse> {
        return try {
            val response = roomHelper.getVillagesByChiefDom(selectedParent)
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getAllPrograms(
        tag: String
    ): Resource<LocalSpinnerResponse> {
        return try {
            val response = roomHelper.getPrograms()
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun registerPatient(hashMap: RequestBody): Resource<RegistrationResponse> {
        return try {
            val response = apiHelper.registerPatient(hashMap)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun validatePatient(
        requestMap: ValidatePatientModel,
        patientCreateReq: Pair<HashMap<String, Any>, List<FormLayout?>?>
    ): Resource<Pair<HashMap<String, Any>, List<FormLayout?>?>> {
        return try {
            val response = apiHelper.validatePatient(requestMap)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource(state = ResourceState.SUCCESS, data = patientCreateReq)
            } else {
                Resource(
                    state = ResourceState.ERROR,
                    message = getErrorMessage(response.errorBody())
                )
            }
        } catch (_: Exception) {
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
}