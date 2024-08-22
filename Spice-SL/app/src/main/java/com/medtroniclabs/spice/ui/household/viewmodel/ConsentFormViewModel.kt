package com.medtroniclabs.spice.ui.household.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.repo.HouseHoldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsentFormViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val houseHoldRepository: HouseHoldRepository
) : ViewModel() {

    private var signatureFilename: String? = null
    private var initialValue: String? = null
    val termsAndConditionStringLiveData = MutableLiveData<String>()

    init {
        viewModelScope.launch(dispatcherIO) {
            termsAndConditionStringLiveData.postValue(houseHoldRepository.getConsentForm().content)
        }
    }

    fun updateSignatureDetails(path: String?, initial: String?) {
        signatureFilename = path
        initialValue = initial
    }


}