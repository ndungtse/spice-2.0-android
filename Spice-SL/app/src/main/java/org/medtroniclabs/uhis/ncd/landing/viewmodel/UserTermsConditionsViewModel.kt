package org.medtroniclabs.uhis.ncd.landing.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ncd.data.TermsAndConditionsModel
import org.medtroniclabs.uhis.ncd.screening.repo.ScreeningRepository
import org.medtroniclabs.uhis.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserTermsConditionsViewModel @Inject constructor(
    private val screeningRepository: ScreeningRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) :
    ViewModel() {
        var updateTermsAndConditionsStatusLiveData =
            MutableLiveData<Resource<TermsAndConditionsModel>>()

        private val userTermsConditionsConsentLiveData = MutableLiveData<String>()
        val consentEntityLiveData: LiveData<String> =
            userTermsConditionsConsentLiveData.switchMap {
                screeningRepository.fetchConsentForm(it)
            }

        fun getConsentForm(formType: String) {
            userTermsConditionsConsentLiveData.value = formType
        }

        fun updateTermsAndConditionsStatus(isAccepted: Boolean) {
            val request = TermsAndConditionsModel(isTermsAndConditionsAccepted = isAccepted)
            viewModelScope.launch(dispatcherIO) {
                updateTermsAndConditionsStatusLiveData.postLoading()
                updateTermsAndConditionsStatusLiveData.postValue(
                    screeningRepository.updateTermsAndConditionsStatus(
                        request,
                    ),
                )
            }
        }
    }
