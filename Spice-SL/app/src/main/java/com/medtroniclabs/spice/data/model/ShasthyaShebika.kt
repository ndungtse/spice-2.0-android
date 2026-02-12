package com.medtroniclabs.spice.data.model

import com.google.gson.annotations.SerializedName

data class ShasthyaShebika(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    
    @SerializedName("ssId")
    val ssId: String? = null,
    
    @SerializedName("shasthyaKormiId")
    val shasthyaKormiId: Long? = null,
    
    @SerializedName("subVillages")
    val subVillages: List<SubVillage>? = null
)
