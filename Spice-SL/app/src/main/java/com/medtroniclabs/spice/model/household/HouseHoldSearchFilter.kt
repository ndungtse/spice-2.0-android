package com.medtroniclabs.spice.model.household

import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.db.dao.HouseholdSortOrder

data class HouseHoldSearchFilter(
    var searchInput: String = "",
    var filterBySs: List<ChipViewItemModel> = listOf(),
    var filterBySubVillages: List<ChipViewItemModel> = listOf(),
    var sortOrder: HouseholdSortOrder = HouseholdSortOrder.DEFAULT,
)
