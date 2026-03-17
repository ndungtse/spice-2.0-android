package org.medtroniclabs.uhis.ui.medicalreview.tb.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.data.model.MotherNeonateAncRequest
import org.medtroniclabs.uhis.data.model.PatientTypeCreateRequest
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientTypeViewModel @Inject constructor(
    private val tbMedicalReviewRepo: TbMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    var lastLocation: Location? = null
    val createPatientType = MutableLiveData<Resource<HashMap<String, Any>>>()
    val getPatientType = MutableLiveData<Resource<HashMap<String, Any>>>()
    var patientTypeChip: ArrayList<ChipViewItemModel> = ArrayList()

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
