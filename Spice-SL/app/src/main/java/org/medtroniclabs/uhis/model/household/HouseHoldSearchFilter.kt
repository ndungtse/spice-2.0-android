package org.medtroniclabs.uhis.model.household

import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.db.dao.HouseholdSortOrder

data class HouseHoldSearchFilter(
    var searchInput: String = "",
    var filterBySs: List<ChipViewItemModel> = listOf(),
    var filterBySubVillages: List<ChipViewItemModel> = listOf(),
    var sortOrder: HouseholdSortOrder = HouseholdSortOrder.DEFAULT,
)
