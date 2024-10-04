package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.db.entity.NCDDiagnosisEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisGetRequest
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisGetResponse
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisRequestResponse
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.mypatients.repo.NCDMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDDiagnosisViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
    private val ncdDiagnosisRepository: NCDMedicalReviewRepo
) : ViewModel() {

    var comments: String = ""
    private val getChips = MutableLiveData<List<String>>()
    var selectedChips: ArrayList<ChipViewItemModel> = ArrayList()
    val getChipLiveData: LiveData<List<NCDDiagnosisEntity>> =
        getChips.switchMap {
            ncdMedicalReviewRepository.getNCDDiagnosisList(it)
        }

    fun getChip(types: List<String>) {
        getChips.value = types
    }

    val createConfirmDiagonsis = MutableLiveData<Resource<HashMap<String, Any>>>()
    val getConfirmDiagonsis = MutableLiveData<Resource<NCDDiagnosisGetResponse>>()

    fun createConfirmDiagonsis(request: NCDDiagnosisRequestResponse) {
        viewModelScope.launch(dispatcherIO) {
            createConfirmDiagonsis.postLoading()
            createConfirmDiagonsis.postValue(ncdDiagnosisRepository.createConfirmDiagonsis(request))
        }
    }

    fun getConfirmDiagonsis(request: NCDDiagnosisGetRequest) {
        viewModelScope.launch(dispatcherIO) {
            getConfirmDiagonsis.postLoading()
            getConfirmDiagonsis.postValue(ncdDiagnosisRepository.getConfirmDiagonsis(request))
        }
    }

}