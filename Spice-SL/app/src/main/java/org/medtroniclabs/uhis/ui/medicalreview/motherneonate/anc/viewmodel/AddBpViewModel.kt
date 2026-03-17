package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.model.BpAndWeightRequestModel
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.repo.MotherNeonateANCRepo
import javax.inject.Inject

@HiltViewModel
class AddBpViewModel @Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    val saveBloodPressure = MutableLiveData<Resource<HashMap<String, Any>>>()
    var lastLocation: Location? = null

    fun saveBloodPressure(bpAndWeightRequestModel: BpAndWeightRequestModel) {
        viewModelScope.launch(dispatcherIO) {
            saveBloodPressure.postLoading()
            saveBloodPressure.postValue(
                motherNeonateANCRepo.createBloodPressure(
                    bpAndWeightRequestModel,
                ),
            )
        }
    }
}
