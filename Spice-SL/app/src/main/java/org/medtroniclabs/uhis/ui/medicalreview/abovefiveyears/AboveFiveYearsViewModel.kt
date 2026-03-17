package org.medtroniclabs.uhis.ui.medicalreview.abovefiveyears

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.data.AboveFiveYearsSummaryDetails
import org.medtroniclabs.uhis.data.AboveFiveYearsSummaryRequest
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.model.MultiSelectDropDownModel
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.PatientListRespModel
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import org.medtroniclabs.uhis.repo.AboveFiveYearsRepository
import org.medtroniclabs.uhis.repo.MedicalReviewSummaryRepository
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboveFiveYearsViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private var repository: AboveFiveYearsRepository,
    private var summaryRepository: MedicalReviewSummaryRepository,
) : BaseViewModel(dispatcherIO) {
    @Inject
    lateinit var connectivityManager: ConnectivityManager
    val aboveFiveYearsMetaLiveData = MutableLiveData<Resource<Boolean>>()
    val summaryDetailsLiveData = MutableLiveData<Resource<AboveFiveYearsSummaryDetails>>()
    val summaryMetaListItems = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()
    val aboveFiveYearsCreateResponse = MutableLiveData<Resource<AboveFiveYearsSummaryDetails>>()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    var selectedPatientStatus: String? = null
    var selectedMedicalSupplyListItem = ArrayList<MultiSelectDropDownModel>()
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
        prescriptionEncounterId: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            aboveFiveYearsCreateResponse.postLoading()
            aboveFiveYearsCreateResponse.postValue(
                repository.createAboveFiveYears(
                    details,
                    selectedComplaintsExaminationsPair,
                    enteredComplaintsExaminationsClinicalNotes,
                    lastLocation,
                    prescriptionEncounterId,
                ),
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
        submitPatientReferenceId: String,
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
                    inUTC = true,
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
                    villageId = villageId,
                )
                summaryCreateResponse.postValue(response)
            }
        }
    }
}
