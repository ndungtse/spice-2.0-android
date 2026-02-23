package com.medtroniclabs.spice.ui.phuwalkins.listener

import com.medtroniclabs.spice.data.offlinesync.model.UnAssignedHouseholdMemberDetail

interface PhuLinkCallback {
    fun onLinkClicked(patientLinkedDetails: Any)

    fun onCallClicked(patientLinkedDetails: UnAssignedHouseholdMemberDetail)
}
