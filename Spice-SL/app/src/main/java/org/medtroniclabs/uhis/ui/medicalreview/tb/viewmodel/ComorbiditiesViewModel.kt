package org.medtroniclabs.uhis.ui.medicalreview.tb.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.repo.ExaminationComplaintsRepository
import javax.inject.Inject

@HiltViewModel
class ComorbiditiesViewModel @Inject constructor(
    @IoDispatcher val dispatcherIO: CoroutineDispatcher,
    private var repository: ExaminationComplaintsRepository,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
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
