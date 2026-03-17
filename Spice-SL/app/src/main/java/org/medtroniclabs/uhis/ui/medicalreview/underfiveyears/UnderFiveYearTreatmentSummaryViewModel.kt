package org.medtroniclabs.uhis.ui.medicalreview.underfiveyears

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.medicalreview.CreateUnderTwoMonthsResponse
import org.medtroniclabs.uhis.model.medicalreview.SummaryDetails
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnderFiveYearTreatmentSummaryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: UnderFiveYearsTreatmentSummaryRepository,
) : ViewModel() {
    val summaryDetailsLiveData = MutableLiveData<Resource<SummaryDetails>>()
    var nextVisitDate: String? = null
    val checkSubmitBtn = MutableLiveData<Boolean>()
    var selectedPatientStatus: String? = null
    private val getMetaPatientStatus = MutableLiveData<String>()

    val metaLiveDataPatientStatus: LiveData<List<MedicalReviewMetaItems>> =
        getMetaPatientStatus.switchMap {
            repository.getExaminationsComplaints(
                it,
                MedicalReviewTypeEnums.UNDER_FIVE_YEARS.name,
            )
        }

    fun getUnderFiveYearsSummaryDetails(request: CreateUnderTwoMonthsResponse) {
        viewModelScope.launch(dispatcherIO) {
            summaryDetailsLiveData.postLoading()
            summaryDetailsLiveData.postValue(
                repository.getUnderFiveYearsSummaryDetails(
                    request,
                ),
            )
        }
    }

    fun setMetaPatientStatus(category: String) {
        getMetaPatientStatus.value = category
    }
}
