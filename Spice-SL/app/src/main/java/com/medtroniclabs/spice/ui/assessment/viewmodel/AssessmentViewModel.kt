package com.medtroniclabs.spice.ui.assessment.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.AssessmentRepository
import com.medtroniclabs.spice.repo.HouseHoldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var houseHoldRepository: HouseHoldRepository,
    private var assessmentRepository: AssessmentRepository
) : ViewModel() {
    var selectedHouseholdMemberId = -1L
    private val assessmentLiveData = MutableLiveData<Resource<Long>>()
    val memberDetailsLiveData = MutableLiveData<Resource<HouseholdMemberEntity>>()
    var menuId: String? = null

    fun getMemberDetailsById() {
        if (selectedHouseholdMemberId == -1L) {
            return
        }
        try {
            viewModelScope.launch(dispatcherIO) {
                memberDetailsLiveData.postLoading()
                val memberEntity =
                    houseHoldRepository.getMemberDetailsByID(selectedHouseholdMemberId)
                memberDetailsLiveData.postSuccess(memberEntity)
            }
        } catch (e: Exception) {
            memberDetailsLiveData.postError()
        }
    }

    fun saveAssessment(resultData: String) {
        viewModelScope.launch(dispatcherIO) {
            try {
                memberDetailsLiveData.value?.data?.householdId?.let {
                    val assessmentEntity = menuId?.let { menuId ->
                        AssessmentEntity(
                            memberId = selectedHouseholdMemberId,
                            householdId = it,
                            assessmentType = menuId.lowercase(),
                            assessmentDetails = resultData,
                            userId = 1
                        )
                    }
                    assessmentEntity?.let {
                        assessmentRepository.saveAssessment(assessmentEntity)
                    }
                    assessmentLiveData.postSuccess()
                }
            } catch (e: Exception) {
                assessmentLiveData.postError()
            }
        }
    }
}