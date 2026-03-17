package org.medtroniclabs.uhis.ui.household.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.repo.HouseHoldRepository
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsentFormViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val houseHoldRepository: HouseHoldRepository,
    private val hivRepository: HivMedicalReviewRepo,
) : BaseViewModel(dispatcherIO) {
    var enableConfirmLiveData = MutableLiveData<Pair<Boolean, Boolean>>()
    val termsAndConditionStringLiveData = MutableLiveData<String>()
    var isHivFlow: Boolean = false
    var isHouseHoldFlow: Boolean = false

    init {
        viewModelScope.launch(dispatcherIO) {
            if (isHivFlow) {
                val content = hivRepository.getConsentForm()?.content ?: ""
                termsAndConditionStringLiveData.postValue(CommonUtils.formatConsent(content))
            } else {
                val content = houseHoldRepository.getConsentForm()?.content ?: ""
                termsAndConditionStringLiveData.postValue(CommonUtils.formatConsent(content))
            }
        }
    }

    fun disableConfirm() {
        enableConfirmLiveData.postValue(Pair(false, false))
    }

    fun enableForInitial(flag: Boolean) {
        val old = enableConfirmLiveData.value ?: Pair(false, false)
        enableConfirmLiveData.postValue(Pair(flag, old.second))
    }

    fun enableForSignature(flag: Boolean) {
        val old = enableConfirmLiveData.value ?: Pair(false, false)
        enableConfirmLiveData.postValue(Pair(old.first, flag))
    }
}
