package com.medtroniclabs.spice.ui.household

import com.medtroniclabs.spice.db.entity.HouseholdListModel

interface HouseholdSelectionListener {
    fun onSelectedPatient(item: HouseholdListModel)
}