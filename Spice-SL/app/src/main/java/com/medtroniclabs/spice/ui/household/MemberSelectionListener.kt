package com.medtroniclabs.spice.ui.household

interface MemberSelectionListener {
    fun onMemberSelected(item: Long, isEdit: Boolean)
}