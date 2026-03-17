package org.medtroniclabs.uhis.ui.phuwalkins.listener

import org.medtroniclabs.uhis.data.offlinesync.model.UnAssignedHouseholdMemberDetail

interface PhuLinkCallback {
    fun onLinkClicked(patientLinkedDetails: Any)

    fun onCallClicked(patientLinkedDetails: UnAssignedHouseholdMemberDetail)
}
