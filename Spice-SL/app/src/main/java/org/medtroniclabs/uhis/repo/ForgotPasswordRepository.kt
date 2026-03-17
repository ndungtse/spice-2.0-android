package org.medtroniclabs.uhis.repo

import com.google.gson.Gson
import org.medtroniclabs.uhis.common.AppConstants.CLIENT_CONSTANT
import org.medtroniclabs.uhis.common.EncryptionUtil
import org.medtroniclabs.uhis.data.ErrorResponse
import org.medtroniclabs.uhis.data.model.RequestChangePassword
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import okhttp3.ResponseBody
import javax.inject.Inject

class ForgotPasswordRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper,
) {
    suspend fun forgotPassword(email: String): Resource<Boolean> =
        try {
            val response = apiHelper.forgotPassword(email, CLIENT_CONSTANT)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = true)
            } else {
                Resource(
                    state = ResourceState.ERROR,
                    message = getErrorMessage(response.errorBody()),
                )
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun verifyToken(token: String): Resource<Boolean> =
        try {
            val response = apiHelper.verifyToken(token)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = true)
            } else {
                Resource(
                    state = ResourceState.ERROR,
                    message = getErrorMessage(response.errorBody()),
                )
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun resetPassword(
        token: String,
        password: String,
    ): Resource<Boolean> =
        try {
            val securedPassword = EncryptionUtil.getSecurePassword(password)
            val response =
                apiHelper.resetPassword(token, RequestChangePassword(securedPassword))
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = true)
            } else {
                Resource(
                    state = ResourceState.ERROR,
                    message = getErrorMessage(response.errorBody()),
                )
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    private fun getErrorMessage(errorBody: ResponseBody?): String? {
        if (errorBody == null) {
            return null
        }
        return try {
            val errorResponse = Gson().fromJson(errorBody.string(), ErrorResponse::class.java)
            return errorResponse.message
        } catch (e: Exception) {
            null
        }
    }
}
