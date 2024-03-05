package com.medtroniclabs.spice.ui.assessment.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.AssessmentRepository
import com.medtroniclabs.spice.repo.MemberRegistrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var memberRegistrationRepository: MemberRegistrationRepository,
    private var assessmentRepository: AssessmentRepository
) : ViewModel() {

    var selectedHouseholdMemberId = -1L
    val assessmentSaveLiveData = MutableLiveData<Resource<String>>()
    val assessmentUpdateLiveData = MutableLiveData<Resource<String>>()
    val memberDetailsLiveData = MutableLiveData<Resource<HouseholdMemberEntity>>()
    var menuId: String? = null
    var workflowName:String? = null
    var formLayout: List<FormLayout>? = null
    var symptomTypeListResponse = MutableLiveData<List<SignsAndSymptomsEntity>>()
    var otherAssessmentDetails = HashMap<String, Any>()
    val formLayoutsLiveData = MutableLiveData<Resource<FormResponse>>()

    fun getMemberDetailsById() {
        if (selectedHouseholdMemberId == -1L) {
            return
        }
        viewModelScope.launch(dispatcherIO) {
            memberRegistrationRepository.getMemberDetailsByID(selectedHouseholdMemberId,memberDetailsLiveData)
        }
    }

    fun saveAssessment(resultData: String) {
        viewModelScope.launch(dispatcherIO) {
            memberDetailsLiveData.value?.data?.householdId?.let { householdId ->
                assessmentRepository.saveAssessment(resultData, householdId, assessmentSaveLiveData, menuId, selectedHouseholdMemberId)
            }
        }
    }

    fun updateOtherAssessmentDetails() {
        viewModelScope.launch(dispatcherIO) {
            assessmentRepository.updateOtherAssessmentDetails(selectedHouseholdMemberId, otherAssessmentDetails, assessmentUpdateLiveData)
        }
    }


    fun insertSignsAndSymptoms() {
        viewModelScope.launch(dispatcherIO) {
            assessmentRepository.insertSymptoms()
        }
    }

    fun getSymptomListByType(type: String) {
        viewModelScope.launch(dispatcherIO) {
            assessmentRepository.getSymptomListByType(type, symptomTypeListResponse)
        }
    }

    fun getFormDataForWorkFlow(formType: String, workflowName: String) {
        viewModelScope.launch(dispatcherIO) {
            assessmentRepository.getFormDataForWorkFlow(formType, workflowName,formLayoutsLiveData)
        }
    }
}