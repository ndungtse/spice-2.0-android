package org.medtroniclabs.uhis.ui.household.viewmodel

import android.content.Intent
import androidx.lifecycle.MediatorLiveData
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
    @param:IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val roomHelper: RoomHelper,
    private val isNonProd: Boolean,
) : BaseViewModel(dispatcherIO) {
    var householdId: Long = -1
        private set
    var memberId: Long = -1
        private set
    var dateOfBirth: String = ""
        private set
    var entryPoint: String? = null
        private set

    val memberDetails = MediatorLiveData<MemberAssessmentHistoryResponse?>()

    fun initialize(intent: Intent) {
        householdId = intent.getLongExtra(DefinedParams.HOUSEHOLD_ID, -1)
        memberId = intent.getLongExtra(DefinedParams.MEMBER_ID, -1)
        dateOfBirth = intent.getStringExtra(DefinedParams.DOB) ?: ""
        entryPoint = intent.getStringExtra(DefinedParams.ENTRY_POINT)
        fetchMemberDetails()
    }

    /**
     * Fetches member details with assessment history from the DB
     */
    private fun fetchMemberDetails() {
        val source = roomHelper.getMemberWithAssessmentHistory(memberId)
        memberDetails.addSource(source) { value ->
            memberDetails.value = value
        }
    }

    fun decrementAssessmentHistoryDate() =
        viewModelScope.launch(dispatcherIO) {
            roomHelper.decrementAssessmentHistoryVisitDate(memberId, 1)
        }

    fun isNonProdEnv() = isNonProd
}
