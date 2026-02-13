package com.medtroniclabs.spice.model.household

import com.medtroniclabs.spice.db.entity.ShasthyaShebikaEntity
import com.medtroniclabs.spice.db.entity.VillageEntity

/**
 * UI filter data for filtering house holds
 */
data class HouseHoldFilterUiData(
    val villageList: List<VillageEntity>,
    val ssList: List<ShasthyaShebikaEntity>,
)
