package com.medtroniclabs.spice.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.data.ErrorResponse
import okhttp3.ResponseBody
import java.lang.reflect.Type

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

    fun convertStringToListOfMap(data: String): ArrayList<Map<String, Any>>? {
        return try {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<Map<String, Any>>>() {}.type
            val map: ArrayList<Map<String, Any>>? = gson.fromJson(data, type)
            map
        } catch (e: Exception) {
            null
        }
    }
}