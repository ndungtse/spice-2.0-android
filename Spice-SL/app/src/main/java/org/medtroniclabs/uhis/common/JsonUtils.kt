package org.medtroniclabs.uhis.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Parses a JSON string to a List
 */
fun <T> parseJsonStringToList(jsonString: String): List<T> =
    try {
        val type: Type = object : TypeToken<List<T>>() {}.type
        Gson().fromJson(jsonString, type) ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
