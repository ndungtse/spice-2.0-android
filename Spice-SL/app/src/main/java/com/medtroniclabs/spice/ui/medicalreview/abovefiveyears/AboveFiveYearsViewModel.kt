package com.medtroniclabs.spice.ui.medicalreview.abovefiveyears

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.MultiSelectDropDownModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.AboveFiveYearsRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.repo.MedicalReviewSummaryRepository
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboveFiveYearsViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private var repository: AboveFiveYearsRepository,
    private var summaryRepository: MedicalReviewSummaryRepository
) : BaseViewModel(dispatcherIO) {

    @Inject
    lateinit var connectivityManager: ConnectivityManager
    val aboveFiveYearsMetaLiveData = MutableLiveData<Resource<Boolean>>()
    val summaryDetailsLiveData = MutableLiveData<Resource<AboveFiveYearsSummaryDetails>>()
    val summaryMetaListItems = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()
    val aboveFiveYearsCreateResponse = MutableLiveData<Resource<AboveFiveYearsSummaryDetails>>()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    var selectedPatientStatus: String? = null
    var selectedMedicalSupplyListItem= ArrayList<MultiSelectDropDownModel>()
    var selectedCostItem: String? = null
    var nextFollowupDate: String? = null
    var lastLocation: Location? = null

    fun getStaticMetaData(menuType: String) {
        viewModelScope.launch(dispatcherIO) {
            aboveFiveYearsMetaLiveData.postLoading()
            aboveFiveYearsMetaLiveData.postValue(repository.getStaticMetaData(menuType))
        }
    }

    fun createAboveFiveYearsResult(
        details: PatientListRespModel,
        selectedComplaintsExaminationsPair: Pair<List<String?>, List<String?>>,
        enteredComplaintsExaminationsClinicalNotes: Triple<String, String, String>,
        prescriptionEncounterId: String?
    ) {
        viewModelScope.launch(dispatcherIO) {
            aboveFiveYearsCreateResponse.postLoading()
            aboveFiveYearsCreateResponse.postValue(
                repository.createAboveFiveYears(
                    details,
                    selectedComplaintsExaminationsPair,
                    enteredComplaintsExaminationsClinicalNotes,
                    lastLocation,
                    prescriptionEncounterId
                )
            )
        }
    }

    fun getSummaryListMetaItems(type: String) {
        viewModelScope.launch(dispatcherIO) {
            summaryMetaListItems.postLoading()
            summaryMetaListItems.postValue(repository.getSummaryDetailMetaItems(type))
        }
    }

    fun getAboveFiveYearsSummaryDetails(request: AboveFiveYearsSummaryRequest) {
        viewModelScope.launch(dispatcherIO) {
            summaryDetailsLiveData.postLoading()
            summaryDetailsLiveData.postValue(repository.getAboveFiveYearsSummaryDetails(request))
        }
    }

    fun aboveFiveYearsSummaryCreate(
        details: PatientListRespModel,
        submitEncounterId: String,
        submitPatientReferenceId: String
    ) {
        viewModelScope.launch(dispatcherIO) {
            summaryCreateResponse.postLoading()
            val medicalSupplyList: List<String>? = selectedMedicalSupplyListItem.takeIf { it.isNotEmpty() }?.mapNotNull { it.value }
            val patientId = details.patientId
            val memberId = details.memberId
            val patientStatus = selectedPatientStatus
            val houseHoldId = details.houseHoldId
            val villageId = details.villageId

            if (patientId != null && memberId != null && patientStatus != null && villageId != null) {
                val nextVisitDate = DateUtils.convertDateTimeToDate(
                    nextFollowupDate,
                    DateUtils.DATE_ddMMyyyy,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    inUTC = true
                )

                val response = summaryRepository.createSummarySubmit(
                    patientId = patientId,
                    patientReference = submitPatientReferenceId,
                    memberId = memberId,
                    id = submitEncounterId,
                    cost = selectedCostItem,
                    medicalSupplies = medicalSupplyList,
                    patientStatus = patientStatus,
                    nextVisitDate = nextVisitDate,
                    referralTicketType = MedicalReviewTypeEnums.ICCM.name,
                    assessmentName = MedicalReviewTypeEnums.ABOVE_FIVE_YEARS.name,
                    householdId = houseHoldId,
                    villageId = villageId
                )
                summaryCreateResponse.postValue(response)
            }
        }
    }
}