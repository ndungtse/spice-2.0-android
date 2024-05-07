package com.medtroniclabs.spice.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.data.ErrorResponse
import okhttp3.ResponseBody

object StringConverter {
    fun convertGivenMapToString(map: HashMap<*, *>?): String? {
        val gson = Gson()
        return gson.toJson(map)
    }

    fun stringToMap(json: String): HashMap<String, Any> {
        val gson = Gson()
        val type = object : TypeToken<HashMap<String, Any>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getErrorMessage(errorBody: ResponseBody?): String? {
        if (errorBody == null)
            return null
        return try {
            val errorResponse =
                Gson().fromJson(errorBody.string(), ErrorResponse::class.java)
            errorResponse?.message
        } catch (e: Exception) {
            null
        }
    }
}