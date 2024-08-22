package com.medtroniclabs.spice.repo

import com.google.gson.Gson
import com.medtroniclabs.spice.common.AppConstants.CLIENT_CONSTANT
import com.medtroniclabs.spice.common.EncryptionUtil
import com.medtroniclabs.spice.data.ErrorResponse
import com.medtroniclabs.spice.data.model.RequestChangePassword
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import okhttp3.ResponseBody
import javax.inject.Inject

class ForgotPasswordRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper
) {

    suspend fun forgotPassword(email: String): Resource<Boolean> {
        return try {
            val response = apiHelper.forgotPassword(email, CLIENT_CONSTANT)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = true)
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

    suspend fun verifyToken(token: String): Resource<Boolean> {
        return try {
            val response = apiHelper.verifyToken(token)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = true)
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

    suspend fun resetPassword(
        token: String,
        password: String
    ): Resource<Boolean> {
        return try {
            val securedPassword = EncryptionUtil.getSecurePassword(password)
            val response =
                apiHelper.resetPassword(token, RequestChangePassword(securedPassword))
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = true)
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
}