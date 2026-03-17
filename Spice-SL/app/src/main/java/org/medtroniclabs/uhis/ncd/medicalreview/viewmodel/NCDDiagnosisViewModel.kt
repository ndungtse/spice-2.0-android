package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.db.entity.NCDDiagnosisEntity
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ncd.data.NCDDiagnosisGetRequest
import org.medtroniclabs.uhis.ncd.data.NCDDiagnosisGetResponse
import org.medtroniclabs.uhis.ncd.data.NCDDiagnosisRequestResponse
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDDiagnosisViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
) : BaseViewModel(dispatcherIO) {
    var comments: String = ""
    private val getChips = MutableLiveData<Triple<List<String>, String, Boolean>>()
    var selectedChips: ArrayList<ChipViewItemModel> = ArrayList()
    val getChipLiveData: LiveData<List<NCDDiagnosisEntity>> =
        getChips.switchMap {
            ncdMedicalReviewRepository.getNCDDiagnosisList(it.first, it.second, it.third)
        }

    fun getChip(
        types: List<String>,
        gender: String,
        isPregnant: Boolean,
    ) {
        getChips.value = Triple(types, gender, isPregnant)
    }

    val createConfirmDiagonsis = MutableLiveData<Resource<HashMap<String, Any>>>()
    val getConfirmDiagonsis = MutableLiveData<Resource<NCDDiagnosisGetResponse>>()

    fun createConfirmDiagonsis(
        request: NCDDiagnosisRequestResponse,
        menuId: String? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            createConfirmDiagonsis.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDConfirmDiagnosisCreation + " " + menuId,
                isCompleted = true,
            )
            createConfirmDiagonsis.postValue(ncdMedicalReviewRepository.createConfirmDiagonsis(request))
        }
    }

    fun getConfirmDiagonsis(request: NCDDiagnosisGetRequest) {
        viewModelScope.launch(dispatcherIO) {
            getConfirmDiagonsis.postLoading()
            getConfirmDiagonsis.postValue(ncdMedicalReviewRepository.getConfirmDiagonsis(request))
        }
    }
}
