package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.Active
import com.medtroniclabs.spice.common.DefinedParams.LIST_LIMIT
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.common.DefinedParams.OnHold
import com.medtroniclabs.spice.common.DefinedParams.OnTreatment
import com.medtroniclabs.spice.common.DefinedParams.REFERRED
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.model.MedicalReviewFilterModel
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.data.PatientVisitRequest
import com.medtroniclabs.spice.ncd.data.PatientVisitResponse
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.mypatients.PatientsDataSource
import com.medtroniclabs.spice.ui.mypatients.repo.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientListViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val apiHelper: ApiHelper,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher
    ) : BaseViewModel(dispatcherIO) {

    //Patient list - Grid count
    var spanCount: Int = DefinedParams.span_count_1
    var totalPatientCount = MutableLiveData<String>()
    var searchText = ""
    var medicalReviewDueTag: List<ChipViewItemModel>? = null
    var patientStatusTag: List<ChipViewItemModel>? = null
    var filterLiveData = MutableLiveData<Boolean>()
    var origin: String? = null
    var selectedPatientDetails: PatientListRespModel? = null
    val patientVisitLiveData = MutableLiveData<Resource<PatientVisitResponse>>()

    val patientsDataSource =
        Pager(config = PagingConfig(pageSize = LIST_LIMIT), pagingSourceFactory = {
            PatientsDataSource(
                apiHelper = apiHelper,
                patientRepository = patientRepository,
                searchText = searchText,
                filter = getFilter(),
                origin = getFormattedOrigin(origin),
                isPatientListRequired = CommonUtils.isPatientListRequired(origin?.lowercase())
            ) { getPatientsCount ->
                totalPatientCount.postValue(getPatientsCount)
            }
        }).flow

    private fun getFormattedOrigin(origin: String?): String? {
        return when (origin?.lowercase()) {
            MenuConstants.SCREENING.lowercase() -> DefinedParams.Screening
            MenuConstants.REGISTRATION.lowercase() -> DefinedParams.Registration
            MenuConstants.ASSESSMENT.lowercase() -> DefinedParams.Assessment
            MenuConstants.MY_PATIENTS_MENU_ID.lowercase() -> DefinedParams.MyPatients
            MenuConstants.DISPENSE.lowercase() -> DefinedParams.Dispense
            MenuConstants.LIFESTYLE.lowercase() -> DefinedParams.Nutritionlifestyle
            MenuConstants.INVESTIGATION.lowercase() -> DefinedParams.Investigation
            else -> null
        }
    }

    fun setFilter(trigger: Boolean) {
        filterLiveData.value = trigger
    }

    private fun getFilter(): MedicalReviewFilterModel? {
        return if (patientStatusTag?.isNullOrEmpty() == false || medicalReviewDueTag?.isNullOrEmpty() == false) {
            MedicalReviewFilterModel(
                patientStatus = patientStatusTag?.map {
                    if (it.name.equals(OnTreatment, true)) OnHold else if (it.name.equals(
                            REFERRED,
                            true
                        )
                    ) {
                        Active
                    } else ""
                },
                visitDate = medicalReviewDueTag?.map { it.name.lowercase() }
            )
        } else {
            null
        }
    }

    fun filterCount(): Int {
        return listOf(patientStatusTag, medicalReviewDueTag).count { !it.isNullOrEmpty() }
    }

    fun createPatientVisit(request: PatientVisitRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientVisitLiveData.postLoading()
            patientVisitLiveData.postValue(
                patientRepository.createPatientVisit(request)
            )
        }
    }
}

