package com.medtroniclabs.spice.ui.boarding.repo

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.EncryptionUtil
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.ErrorResponse
import com.medtroniclabs.spice.data.LoginResponse
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
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
                loginResponseLiveDta.postSuccess(response.body())
                val headers = response.headers().toMultimap()
                val loginResponseModel = response.body()
                saveTokenInformation(headers)
                loginResponseModel?.let {
                    saveUserNameAndPassword(username, securePassword)
                }
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
}