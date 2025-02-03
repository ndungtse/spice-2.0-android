package com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.ExaminationComplaintsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComorbiditiesViewModel @Inject constructor(
    @IoDispatcher val dispatcherIO: CoroutineDispatcher,
    private var repository: ExaminationComplaintsRepository,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository
) : ViewModel() {

    var chips: ArrayList<ChipViewItemModel> = ArrayList()
    val getChipItems = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()
    var comments: String = ""

    fun getChips(type: String) {
        viewModelScope.launch(dispatcherIO) {
            getChipItems.postLoading()
            getChipItems.postValue(repository.getComplaintsListByType(type))
        }
    }

}