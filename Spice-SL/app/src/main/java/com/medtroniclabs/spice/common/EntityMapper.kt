package com.medtroniclabs.spice.common

import com.medtroniclabs.spice.data.CountryModel
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.ProgramEntity
import com.medtroniclabs.spice.db.entity.ChiefDomEntity
import com.medtroniclabs.spice.db.entity.DistrictEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.ShasthyaShebikaEntity
import com.medtroniclabs.spice.db.entity.SubVillageEntity
import com.medtroniclabs.spice.db.entity.VillageEntity

object EntityMapper {
    fun getResultSpinnerMapList(data: LocalSpinnerResponse): ArrayList<Map<String, Any>> {
        if (data.response is List<*>) {
            return ArrayList(
                data.response.map { properties ->
                    val map = HashMap<String, Any>()
                    mapsIdName(properties, map)
                    map
                },
            )
        }
        return ArrayList()
    }

    private fun mapsIdName(
        properties: Any?,
        map: HashMap<String, Any>,
    ) {
        when (properties) {
            is VillageEntity -> {
                updateMapsIdName(map, properties.id, properties.name)
            }
            is HealthFacilityEntity -> {
                updateMapsIdName(map, properties.name, properties.name)
            }
            is CountryModel -> {
                updateMapsIdName(map, properties.id, properties.name)
            }

            is ChiefDomEntity -> {
                updateMapsIdName(map, properties.id, properties.name)
            }
            is DistrictEntity -> {
                updateMapsIdName(map, properties.id, properties.name)
            }

            is ProgramEntity -> {
                updateMapsIdName(map, properties.id, properties.name)
            }
            is ShasthyaShebikaEntity -> {
                // Format as "[ssid] [name]" if ssId exists, otherwise just use name
                val displayName = if (!properties.ssId.isNullOrBlank()) {
                    "${properties.ssId}-${properties.name}"
                } else {
                    properties.name
                }
                updateMapsIdName(map, properties.id, displayName)
            }
            is SubVillageEntity -> {
                updateMapsIdName(map, properties.id, properties.name)
            }
            is HouseholdMemberEntity -> {
                updateMapsIdName(map, properties.id, properties.name)
            }
        }
    }

    private fun updateMapsIdName(
        map: HashMap<String, Any>,
        id: Any,
        name: String,
    ) {
        map[DefinedParams.ID] = id
        map[DefinedParams.NAME] = name
    }
}
