package com.medtroniclabs.spice.app.analytics.utils

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object CommonUtils {

    fun stringToJsonElement(jsonString: String): JsonElement {
        return JsonParser.parseString(jsonString)
    }

    fun jsonElementToString(jsonElement: JsonElement): String {
        return Gson().toJson(jsonElement)
    }

    fun mapToString(map: Map<String,Any>): String {
        return Gson().toJson(map)
    }

    fun stringToMap(input: String): Map<String, Any> {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return  Gson().fromJson(input, mapType)
    }

    fun getCurrentDateTimeInUTC(): String {
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(currentTime)
    }

    fun getAnalyticsFileName(): String {
        val strBuilder = StringBuilder()
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        strBuilder.append(dateFormat.format(currentTime))
        strBuilder.append("_")
        strBuilder.append(UserDetail.userId)
        strBuilder.append("_")
        strBuilder.append("analytics.json")
        return strBuilder.toString()
    }
}