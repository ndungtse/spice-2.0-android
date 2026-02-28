package com.medtroniclabs.spice.model.household

import com.medtroniclabs.spice.db.entity.ShasthyaShebikaEntity
import com.medtroniclabs.spice.db.entity.SubVillageEntity

/**
 * UI filter data for filtering house holds
 */
data class HouseHoldFilterUiData(
    val ssList: List<ShasthyaShebikaEntity>,
    val subVillages: List<SubVillageEntity>,
)
