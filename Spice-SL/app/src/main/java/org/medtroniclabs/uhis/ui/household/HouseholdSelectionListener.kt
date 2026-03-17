package org.medtroniclabs.uhis.ui.household

interface HouseholdSelectionListener {
    fun onHouseHoldSelected(id: Long)

    fun filterHouseholdList()
}
