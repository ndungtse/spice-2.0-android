package com.medtroniclabs.spice.model.services

import com.medtroniclabs.spice.data.model.ChipViewItemModel

data class ServicesSearchFilter(
    var searchInput: String = "",
    var filterBySs: List<ChipViewItemModel> = listOf(),
    var filterBySubVillages: List<ChipViewItemModel> = listOf(),
    var staticFilter: ServiceStaticFilter = ServiceStaticFilter.ALL_MEMBERS,
)
