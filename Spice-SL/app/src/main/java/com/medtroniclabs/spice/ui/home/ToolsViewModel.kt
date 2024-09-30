package com.medtroniclabs.spice.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.PatientVisitRequest
import com.medtroniclabs.spice.ncd.data.PatientVisitResponse
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.boarding.MenuTypeEnums
import com.medtroniclabs.spice.ui.boarding.repo.MetaRepository
import com.medtroniclabs.spice.ui.mypatients.repo.NCDMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToolsViewModel @Inject constructor(
    private val metaRepository: MetaRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var ncdMedicalReviewRepo: NCDMedicalReviewRepo
) : ViewModel() {

    var selectedHouseholdMemberID = -1L
    var selectedMemberDob: String? = null
    var followUpId = -1L
    var menuId: String? = null
    val resultRMNCHFlowHashMap = HashMap<String, Any>()
    val menuListLiveData = MutableLiveData<Resource<List<MenuEntity>>>()
    val resultANCFlowHashMap = HashMap<String, Any>()
    val patientVisitLiveData = MutableLiveData<Resource<PatientVisitResponse>>()

    fun getMenuForClinicalWorkflows() {
        viewModelScope.launch(dispatcherIO) {
            menuListLiveData.postLoading()
            menuListLiveData.postValue(
                metaRepository.getMenuForClinicalWorkflows(
                    selectedHouseholdMemberID
                )
            )
        }
    }

    fun getMenuClinicalWorkflows(gender: String) {
        viewModelScope.launch(dispatcherIO) {
            menuListLiveData.postLoading()
            menuListLiveData.postValue(
                metaRepository.getMenuClinicalWorkflows(
                    gender, MenuTypeEnums.assessment.name
                )
            )
        }
    }

    fun getMyPatientsMenuItemsList() {
        viewModelScope.launch(dispatcherIO) {
            menuListLiveData.postLoading()
            menuListLiveData.postValue(
                metaRepository.getMenu()
            )
        }
    }

    fun createPatientVisit(request: PatientVisitRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientVisitLiveData.postLoading()
            patientVisitLiveData.postValue(
                ncdMedicalReviewRepo.createPatientVisit(request)
            )
        }
    }

}