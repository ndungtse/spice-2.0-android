package com.medtroniclabs.spice.common

import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.VillageEntity

object EntityMapper {

    fun getResultSpinnerMapList(data: LocalSpinnerResponse): ArrayList<Map<String, Any>> {
        if (data.response is List<*>) {
            return ArrayList(data.response.map { properties ->
                val map = HashMap<String, Any>()
                mapsIdName(properties, map)
                map
            })
        }
        return ArrayList()
    }

    private fun mapsIdName(properties: Any?, map: HashMap<String, Any>) {
        when (properties) {
            is VillageEntity -> {
                updateMapsIdName(map, properties.id, properties.name)
            }
            is HealthFacilityEntity -> {
                updateMapsIdName(map,properties.name,properties.name)
            }
        }
    }

    private fun updateMapsIdName(map: HashMap<String, Any>, id: Any, name: String) {
        map[DefinedParams.ID] = id
        map[DefinedParams.NAME] = name
    }

}