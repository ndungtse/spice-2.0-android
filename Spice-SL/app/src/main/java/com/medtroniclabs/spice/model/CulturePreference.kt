package com.medtroniclabs.spice.model

import com.medtroniclabs.spice.data.CulturesEntity

data class CulturePreference(
    var cultureId: Long,
    var name: String,
    var isTranslationEnabled: Boolean
)

data class CultureLocaleModel(
    val id: Long,
    val culture: CulturesEntity
)