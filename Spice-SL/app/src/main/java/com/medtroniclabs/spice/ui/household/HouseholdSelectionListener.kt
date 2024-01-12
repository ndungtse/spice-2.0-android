package com.medtroniclabs.spice.ui.household

import com.medtroniclabs.spice.db.entity.HouseholdEntity

interface HouseholdSelectionListener {
    fun onHouseHoldSelected(item: HouseholdEntity)
}