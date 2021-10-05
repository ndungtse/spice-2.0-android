package com.mdtlabs.ncd.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


object StringConverter {

    fun convertGivenStringToMap(data: String): Map<String, Any>? {
        return try {
            val gson = Gson()
            val type: Type = object : TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any>? = gson.fromJson(data, type)
            map
        } catch (e: Exception) {
            null
        }
    }


    fun convertGivenMapToString(map: HashMap<*, *>?): String? {
        val gson = Gson()
        return gson.toJson(map)
    }

}