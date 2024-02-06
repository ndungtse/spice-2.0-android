package com.medtroniclabs.spice.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
}