package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.model.BpAndWeightRequestModel
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.repo.MotherNeonateANCRepo
import javax.inject.Inject

@HiltViewModel
class AddWeightViewModel @Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    val saveWeight = MutableLiveData<Resource<HashMap<String, Any>>>()
    var lastLocation: Location? = null

    fun saveWeight(bpAndWeightRequestModel: BpAndWeightRequestModel) {
        viewModelScope.launch(dispatcherIO) {
            saveWeight.postLoading()
            saveWeight.postValue(motherNeonateANCRepo.createWeight(bpAndWeightRequestModel))
        }
    }
}
