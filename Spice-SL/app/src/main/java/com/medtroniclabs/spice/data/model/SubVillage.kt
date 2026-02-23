package com.medtroniclabs.spice.data.model

import com.google.gson.annotations.SerializedName

data class SubVillage(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("villageId")
    val villageId: Long,
)
