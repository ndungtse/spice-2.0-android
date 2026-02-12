package com.medtroniclabs.spice.ui.household.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.repo.HouseHoldRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsentFormViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val houseHoldRepository: HouseHoldRepository,
    private val hivRepository: HivMedicalReviewRepo

) : BaseViewModel(dispatcherIO) {

    var enableConfirmLiveData = MutableLiveData<Pair<Boolean, Boolean>>()
    val termsAndConditionStringLiveData = MutableLiveData<String>()
    var isHivFlow : Boolean = false
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