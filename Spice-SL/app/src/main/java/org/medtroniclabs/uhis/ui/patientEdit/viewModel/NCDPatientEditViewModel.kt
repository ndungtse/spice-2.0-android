package org.medtroniclabs.uhis.ui.patientEdit.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.patientEdit.NCDPatientEditRepository
import javax.inject.Inject

@HiltViewModel
class NCDPatientEditViewModel @Inject constructor(
    private val repository: NCDPatientEditRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    var updatePatientMap = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun ncdUpdatePatientDetail(request: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            updatePatientMap.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDPatientEdit,
                isCompleted = true,
            )
            updatePatientMap.postValue(repository.ncdUpdatePatientDetail(request))
        }
    }
}
