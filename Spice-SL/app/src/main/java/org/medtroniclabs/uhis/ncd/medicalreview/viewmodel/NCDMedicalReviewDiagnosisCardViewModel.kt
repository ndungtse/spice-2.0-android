package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ncd.data.NCDDiagnosisGetRequest
import org.medtroniclabs.uhis.ncd.data.NCDDiagnosisGetResponse
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import org.medtroniclabs.uhis.network.resource.Resource
import javax.inject.Inject

@HiltViewModel
class NCDMedicalReviewDiagnosisCardViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
) : ViewModel() {
    val getConfirmDiagonsis = MutableLiveData<Resource<NCDDiagnosisGetResponse>>()

    fun getConfirmDiagonsis(request: NCDDiagnosisGetRequest) {
        viewModelScope.launch(dispatcherIO) {
            getConfirmDiagonsis.postLoading()
            getConfirmDiagonsis.postValue(ncdMedicalReviewRepository.getConfirmDiagonsis(request))
        }
    }
}
