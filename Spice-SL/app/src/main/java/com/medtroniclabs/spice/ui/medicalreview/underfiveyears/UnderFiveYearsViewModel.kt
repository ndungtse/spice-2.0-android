package com.medtroniclabs.spice.ui.medicalreview.underfiveyears

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.UnderFiveYearsRepository
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnderFiveYearsViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var underFiveYearsRepository: UnderFiveYearsRepository
) : ViewModel() {

    val underFiveYearsMetaLiveData = MutableLiveData<Resource<Boolean>>()
    var patientId: String? = null

    fun getStaticMetaData() {
        viewModelScope.launch(dispatcherIO) {
            underFiveYearsMetaLiveData.postLoading()
            underFiveYearsMetaLiveData.postValue(underFiveYearsRepository.getStaticMetaData(MedicalReviewTypeEnums.UnderFiveYears.name))
        }
    }
}