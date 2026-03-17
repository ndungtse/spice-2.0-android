package org.medtroniclabs.uhis.ui.medicalreview.tb.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.model.BpAndWeightRequestModel
import org.medtroniclabs.uhis.data.model.BpAndWeightResponse
import org.medtroniclabs.uhis.data.model.MotherNeonateAncRequest
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BmiViewModel @Inject constructor(
    private val tbMedicalReviewRepo: TbMedicalReviewRepo,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    var lastLocation: Location? = null
    val getBmiList = MutableLiveData<Resource<List<BpAndWeightResponse>>>()

    fun fetchBmiList(motherNeonateAncRequest: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getBmiList.postLoading()
            getBmiList.postValue(tbMedicalReviewRepo.fetchBmiList(motherNeonateAncRequest))
        }
    }

    val saveBMI = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun saveBMI(bpAndWeightRequestModel: BpAndWeightRequestModel) {
        viewModelScope.launch(dispatcherIO) {
            saveBMI.postLoading()
            saveBMI.postValue(
                tbMedicalReviewRepo.createBMI(
                    bpAndWeightRequestModel,
                ),
            )
        }
    }
}
