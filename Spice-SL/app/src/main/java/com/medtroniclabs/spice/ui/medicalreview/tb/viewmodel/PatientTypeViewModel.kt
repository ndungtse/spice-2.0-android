package com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.PatientTypeCreateRequest
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientTypeViewModel @Inject constructor(
    private val tbMedicalReviewRepo: TbMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var lastLocation:Location? = null
    val createPatientType = MutableLiveData<Resource<HashMap<String, Any>>>()
    val getPatientType = MutableLiveData<Resource<HashMap<String, Any>>>()
    var patientTypeChip:ArrayList<ChipViewItemModel> = ArrayList()

    private val getPatientTypeMeta = MutableLiveData<String>()
    val getPatientTypeLiveData: LiveData<List<MedicalReviewMetaItems>> =
        getPatientTypeMeta.switchMap {
            tbMedicalReviewRepo.getExaminationsComplaints(it, MedicalReviewTypeEnums.TB.name)
        }

    fun setPatientType(category: String) {
        getPatientTypeMeta.value = category
    }

    fun createPatientType(request: PatientTypeCreateRequest) {
        viewModelScope.launch(dispatcherIO) {
            createPatientType.postLoading()
            createPatientType.postValue(tbMedicalReviewRepo.createPatientType(request))
        }
    }

    fun getPatientType(request: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getPatientType.postLoading()
            getPatientType.postValue(tbMedicalReviewRepo.getPatientType(request))
        }
    }
}