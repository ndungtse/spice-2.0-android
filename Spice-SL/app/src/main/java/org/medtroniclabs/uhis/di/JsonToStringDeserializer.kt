package org.medtroniclabs.uhis.di

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Converts a given Json to string
 */
class JsonToStringDeserializer : JsonDeserializer<String> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): String? =
        when {
            json == null || json.isJsonNull -> null
            json.isJsonObject || json.isJsonArray -> json.toString()
            json.isJsonPrimitive -> json.asString
            else -> json.toString()
        }
}
