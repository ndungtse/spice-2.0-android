package org.medtroniclabs.uhis.ui.household.viewmodel

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.db.response.MemberAssessmentHistoryResponse
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ui.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class MemberSummaryViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val roomHelper: RoomHelper,
) : BaseViewModel(dispatcherIO) {
    var householdId: Long = -1
        private set
    var memberId: Long = -1
        private set
    var dateOfBirth: String = ""
        private set

    val memberDetails = MutableLiveData<MemberAssessmentHistoryResponse?>()

    fun initialize(intent: Intent) {
        householdId = intent.getLongExtra(DefinedParams.HOUSEHOLD_ID, -1)
        memberId = intent.getLongExtra(DefinedParams.MEMBER_ID, -1)
        dateOfBirth = intent.getStringExtra(DefinedParams.DOB) ?: ""
        fetchMemberDetails()
    }

    /**
     * Fetches member details with assessment history from the DB
     */
    private fun fetchMemberDetails() =
        viewModelScope.launch(dispatcherIO) {
            memberDetails.postValue(roomHelper.getMemberWithAssessmentHistory(memberId))
        }
}
