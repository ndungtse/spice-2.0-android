package org.medtroniclabs.uhis.ui.household.viewmodel

import android.content.Intent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ui.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class MemberSummaryViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    var householdId: Long = -1
        private set
    var memberId: Long = -1
        private set
    var dateOfBirth: String = ""
        private set

    fun initialize(intent: Intent) {
        householdId = intent.getLongExtra(DefinedParams.HOUSEHOLD_ID, -1)
        memberId = intent.getLongExtra(DefinedParams.MEMBER_ID, -1)
        dateOfBirth = intent.getStringExtra(DefinedParams.DOB) ?: ""
    }
}
