package com.medtroniclabs.spice.model.household

import com.medtroniclabs.spice.data.model.ChipViewItemModel

data class HouseHoldSearchFilter(
    var searchInput: String = "",
    var filterByVillage: List<ChipViewItemModel> = listOf(),
    var filterByStatus: List<ChipViewItemModel> = listOf(),
    var filterBySs: List<ChipViewItemModel> = listOf()
)
