package org.medtroniclabs.uhis.model

import org.medtroniclabs.uhis.data.CulturesEntity

data class CulturePreference(
    var cultureId: Long,
    var name: String,
    var isTranslationEnabled: Boolean,
)

data class CultureLocaleModel(
    val id: Long,
    val culture: CulturesEntity,
)
