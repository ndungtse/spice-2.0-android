package com.medtroniclabs.spice.ui.patientDelete.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.ShortageReasonEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.NCDPatientRemoveRequest
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.mypatients.repo.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDPatientDeleteViewModel @Inject constructor(
    private var ncdMedicalReviewRepo: NCDMedicalReviewRepository,
    private val patientRepository: PatientRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    val deleteReasonList = MutableLiveData<List<ShortageReasonEntity>>()
    val patientRemoveResponse = MutableLiveData<Resource<Boolean>>()
    fun getDeleteReasonList() {
        viewModelScope.launch(dispatcherIO) {
            val deleteList = ncdMedicalReviewRepo.getNCDShortageReason(NCDMRUtil.TYPE_DELETE)
            val list = ArrayList(deleteList)
            if (list.isNotEmpty()) {
                val itemIndex =
                    list.indexOfFirst { it.name.contains(DefinedParams.Other, ignoreCase = true) }
                if (itemIndex >= 0 && (itemIndex + 1) != list.size) {
                    val item = list.removeAt(itemIndex)
                    list.add(item)
                }
            }
            deleteReasonList.postValue(list)
        }
    }

    fun ncdPatientRemove(request: NCDPatientRemoveRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientRemoveResponse.postLoading()
            patientRemoveResponse.postValue(patientRepository.ncdPatientRemove(request))
        }
    }

}