package com.medtroniclabs.spice.common

import com.google.gson.Gson

object StringConverter {
    fun convertGivenMapToString(map: HashMap<*, *>?): String? {
        val gson = Gson()
        return gson.toJson(map)
    }
}