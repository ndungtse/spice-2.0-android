package com.medtroniclabs.spice.ui.patientEdit.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.patientEdit.NCDPatientEditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDPatientEditViewModel @Inject constructor(
    private val repository: NCDPatientEditRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var updatePatientMap = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun ncdUpdatePatientDetail(request: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            updatePatientMap.postLoading()
            updatePatientMap.postValue(repository.ncdUpdatePatientDetail(request))
        }
    }

}