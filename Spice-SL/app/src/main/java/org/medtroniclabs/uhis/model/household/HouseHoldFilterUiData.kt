package org.medtroniclabs.uhis.model.household

import org.medtroniclabs.uhis.db.entity.ShasthyaShebikaEntity
import org.medtroniclabs.uhis.db.entity.SubVillageEntity

/**
 * UI filter data for filtering house holds
 */
data class HouseHoldFilterUiData(
    val ssList: List<ShasthyaShebikaEntity>,
    val subVillages: List<SubVillageEntity>,
)
