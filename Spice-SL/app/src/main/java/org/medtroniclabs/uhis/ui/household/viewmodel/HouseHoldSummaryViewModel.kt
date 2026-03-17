package org.medtroniclabs.uhis.ui.household.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.data.offlinesync.model.HouseholdMemberWithTb
import org.medtroniclabs.uhis.db.response.HouseHoldEntityWithLastActivity
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.repo.HouseHoldRepository
import org.medtroniclabs.uhis.repo.HouseholdMemberRepository
import org.medtroniclabs.uhis.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseHoldSummaryViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val houseHoldRepository: HouseHoldRepository,
    private var memberRegistrationRepository: HouseholdMemberRepository,
) : BaseViewModel(dispatcherIO) {
    var houseHoldId: Long = -1L
    var isFromHouseHoldRegistration: Boolean = false
    var selectedMemberId = -1L
    var villageId: Long = -1L
    var previousCount: Int = 0
    var selectedMemberDob: String? = null
    var isPhuWalkInsFlow: Boolean = false
    var hasDeceasedReason: Boolean = false

    private val houseHoldNoLiveData = MutableLiveData<Long>()
    val householdCardDetailLiveData: LiveData<List<HouseHoldEntityWithLastActivity>> =
        houseHoldNoLiveData.switchMap { id ->
            houseHoldRepository.getHouseholdCardDetailLiveData(id)
        }

    val householdMembersLiveData: LiveData<List<HouseholdMemberWithTb>> =
        houseHoldNoLiveData.switchMap { id ->
            houseHoldRepository.getAllHouseHoldMembersLiveData(id)
        }

    fun setHouseholdId(hhId: Long) {
        this.houseHoldId = hhId
        houseHoldNoLiveData.value = hhId
    }

    fun updateMemberDeceasedReason(
        id: Long,
        status: Boolean,
        deceasedReason: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            memberRegistrationRepository.updateMemberDeceasedReason(
                id,
                status,
                deceasedReason,
            )
        }
    }
}
