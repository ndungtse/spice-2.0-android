package com.medtroniclabs.spice.ncd.counseling.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.RoleConstant
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.APIResponse
import com.medtroniclabs.spice.db.entity.NCDMedicalReviewMetaEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.PatientLifestyle
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ncd.counseling.model.NCDCounselingModel
import com.medtroniclabs.spice.ncd.counseling.model.AssessmentResultModel
import com.medtroniclabs.spice.ncd.counseling.repo.CounselingRepo
import com.medtroniclabs.spice.network.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CounselingViewModel @Inject constructor(
    private val counselingRepo: CounselingRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) :
    ViewModel() {
    var patientReference: String? = null
    var memberReference: String? = null
    var encounterReference: String? = null

    //Lifestyle [Provider & Nutritionist]
    var lifestyles: List<String>? = null
    var clinicianNote: String? = null
    var lifestyleAssessment: String? = null
    var otherNote: String? = null

    //Psychological [Provider & Counselor]
    var clinicianNotes: ArrayList<String>? = null
    var counselorAssessment: String? = null

    var nutritionist: Boolean? = null
    var counselor: Boolean? = null

    init {
        SecuredPreference.getUserDetails()?.roles?.joinToString { it.name }?.let { userRole ->
            nutritionist = userRole.contains(RoleConstant.NUTRITIONIST)
            counselor = userRole.contains(RoleConstant.COUNSELOR)
        }
    }

    var createAssessmentLiveData =
        SingleLiveEvent<Resource<APIResponse<NCDCounselingModel>>>()
    var updateAssessmentLiveData =
        MutableLiveData<Resource<APIResponse<NCDCounselingModel>>>()
    var assessmentListLiveData =
        MutableLiveData<Resource<APIResponse<ArrayList<NCDCounselingModel>>>>()
    var removeAssessmentLiveData =
        MutableLiveData<Resource<APIResponse<NCDCounselingModel>>>()

    fun createAssessment(request: NCDCounselingModel, lifestyle: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            createAssessmentLiveData.postLoading()
            createAssessmentLiveData.postValue(counselingRepo.createAssessment(request, lifestyle))
        }
    }

    fun updateAssessment(request: AssessmentResultModel, lifestyle: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            updateAssessmentLiveData.postLoading()
            updateAssessmentLiveData.postValue(counselingRepo.updateAssessment(request, lifestyle))
        }
    }

    fun getAssessmentList(request: NCDCounselingModel, lifestyle: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            assessmentListLiveData.postLoading()
            assessmentListLiveData.postValue(counselingRepo.getAssessmentList(request, lifestyle))
        }
    }

    fun removeAssessment(request: NCDCounselingModel, lifestyle: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            removeAssessmentLiveData.postLoading()
            removeAssessmentLiveData.postValue(counselingRepo.removeAssessment(request, lifestyle))
        }
    }

    private val getChip = MutableLiveData<Boolean>()
    fun getChips() {
        getChip.value = true
    }

    val getChipItems: LiveData<List<NCDMedicalReviewMetaEntity>> = getChip.switchMap {
        counselingRepo.getLifestyleAssessments(category = PatientLifestyle)
    }
}