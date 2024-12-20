package com.medtroniclabs.spice.ncd.followup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMdd_HHmmss
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.db.entity.NCDCallDetails
import com.medtroniclabs.spice.db.entity.NCDFollowUp
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.SortModel
import com.medtroniclabs.spice.ncd.data.CustomDate
import com.medtroniclabs.spice.ncd.data.FollowUpUpdateRequest
import com.medtroniclabs.spice.ncd.data.PatientFollowUpEntity
import com.medtroniclabs.spice.ncd.data.RegisterCallResponse
import com.medtroniclabs.spice.ncd.data.SortModelForFollowUp
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils
import com.medtroniclabs.spice.ncd.followup.adapter.NCDFollowUpDataSource
import com.medtroniclabs.spice.ncd.followup.fragment.NCDFollowUpFilterEnum
import com.medtroniclabs.spice.ncd.followup.repo.NCDFollowUpRepo
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.SingleLiveEvent
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class NCDFollowUpViewModel @Inject constructor(
    private val apiHelper: ApiHelper,
    private val ncdFollowUpRepo: NCDFollowUpRepo,
    private var roomHelper: RoomHelper,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher
) : BaseViewModel(dispatcherIO) {
    var spanCount: Int = DefinedParams.span_count_1
    var searchText = ""
    var type = ""
    var sortModel : SortModelForFollowUp? = null
    var customDate: CustomDate? = null
    var dateRange: String? = null
    var totalPatientCount = MutableLiveData<Int?>()
    var filterCount = MutableLiveData<Int>()
    var filterSet = MutableLiveData<Boolean>()
    var getPatientRegisterResponse = SingleLiveEvent<Resource<RegisterCallResponse>>()
    var statusUpdateResponse = SingleLiveEvent<Resource<HashMap<String, Any>>>()
    var remainingAttempts = listOf<ChipViewItemModel>()
    var selectedPatient: PatientFollowUpEntity? = null
    val callResultHashMap = HashMap<String, Any>()
    val patientStatusHashMap = HashMap<String, Any>()
    val unSuccessfulHashMap = HashMap<String, Any>()

    val patientsDataSource =
        Pager(config = PagingConfig(pageSize = DefinedParams.LIST_LIMIT), pagingSourceFactory = {
            NCDFollowUpDataSource(
                apiHelper = apiHelper,
                ncdFollowUpRepo = ncdFollowUpRepo,
                roomHelper = roomHelper,
                searchText = searchText,
                sortModel = sortModel,
                customDate = if (dateRange == NCDFollowUpFilterEnum.CUSTOMISE.title) customDate else null,
                dateRange = dateRange,
                type = type,
                remainingAttempts = remainingAttempts.mapNotNull { it.id }
            ) { getPatientsCount ->
                totalPatientCount.postValue(getPatientsCount)
            }
        }).flow


    fun getPatientCallRegister() {
        viewModelScope.launch(dispatcherIO) {
            getPatientRegisterResponse.postLoading()
            getPatientRegisterResponse.postValue(ncdFollowUpRepo.getPatientCallRegister())
        }
    }


    fun updatePatientCallRegister(request: FollowUpUpdateRequest) {
        viewModelScope.launch(dispatcherIO) {
            statusUpdateResponse.postLoading()
            statusUpdateResponse.postValue(ncdFollowUpRepo.updatePatientCallRegister(request))
        }
    }

    fun filterLiveData() {
        filterSet.postValue(true)
        val count = listOfNotNull(
            dateRange.takeIf { !it.isNullOrBlank() },
            remainingAttempts.takeIf { !it.isNullOrEmpty() }
        ).size

        if (count > 0) {
            filterCount.postValue(count)
        } else {
            filterCount.postValue(0)
        }
    }


    // Follow up Offline
    var selectedFollowUpPatient: NCDFollowUp? = null
    private var searchTextOffline: String = ""
    fun searchLiveDataForOffline(text: String) {
        this.searchTextOffline = text
        searchTextOfflineLiveData.value = true
    }

    private val searchTextOfflineLiveData = MutableLiveData<Boolean>()
    val getFollowUpData: LiveData<List<NCDFollowUp>> = searchTextOfflineLiveData.switchMap {
        ncdFollowUpRepo.getNCDFollowUpData(
            filterByVillage.map { it.id?.toString().orEmpty() }
                .takeIf { list -> list.any { it.isNotBlank() } },
            typeOffline,
            searchTextOffline,
            getDateBasedOnChip(),
            sortTriple.first,
            sortTriple.second
        )
    }
    var typeOffline = ""
    var totalPatientCountOffline = MutableLiveData<Int>()

    val saveCallDetails = MutableLiveData<Resource<NCDCallDetails?>>()
    val updateCallLiveData = MutableLiveData<Resource<NCDFollowUp>>()
    val getInitialLiveData = MutableLiveData<Resource<NCDFollowUp?>>()

    var filterByVillage: List<ChipViewItemModel> = listOf()
    var filterByDateRange: List<ChipViewItemModel> = listOf()
    var data: NCDFollowUp? = null


    fun filterFollowUpOfflineLiveData() {
        searchTextOfflineLiveData.value = true
        val count = listOfNotNull(
            filterByDateRange.takeIf { !it.isNullOrEmpty() },
            filterByVillage.takeIf { !it.isNullOrEmpty() }
        ).size

        if (count > 0) {
            filterCount.postValue(count)
        } else {
            filterCount.postValue(0)
        }
    }

    fun insertNCDCallDetails(followUp: NCDCallDetails) {
        viewModelScope.launch {
            try {
                saveCallDetails.postLoading()
                val updatedFollowUp = ncdFollowUpRepo.insertNCDCallDetails(followUp)
                saveCallDetails.postSuccess(updatedFollowUp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateInitial(value: NCDFollowUp) {
        viewModelScope.launch(dispatcherIO) {
            try {
                updateCallLiveData.postLoading()
                val updatedFollowUp = ncdFollowUpRepo.updatedCallInitiatedCall(value)
                updateCallLiveData.postSuccess(updatedFollowUp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getInitial() {
        viewModelScope.launch(dispatcherIO) {
            try {
                getInitialLiveData.postLoading()
                val updatedFollowUp = ncdFollowUpRepo.getNCDInitiatedCallFollowUp()
                getInitialLiveData.postSuccess(updatedFollowUp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var villageListResponse = MutableLiveData<Resource<List<VillageEntity>>>()
    fun getAllVillagesName() {
        viewModelScope.launch(dispatcherIO) {
            villageListResponse.postLoading()
            villageListResponse.postValue(ncdFollowUpRepo.getAllVillagesName())
        }
    }

    private fun getDateBasedOnChip(): Pair<Long?, Long?>? {
        var result: Pair<Long?, Long?>? = null

        val currentDay = DateUtils.getCurrentDayMonthYear()
        val startOfDay =
            LocalDateTime.of(currentDay.third, currentDay.second, currentDay.first, 0, 0, 0)
        val endOfDay =
            LocalDateTime.of(currentDay.third, currentDay.second, currentDay.first, 23, 59, 59)

        val startMillis = startOfDay.toInstant(ZoneOffset.UTC).toEpochMilli()
        val endMillis = endOfDay.toInstant(ZoneOffset.UTC).toEpochMilli()
        val oneDayMillis = 24 * 60 * 60 * 1000

        if (filterByDateRange.isEmpty()) {
            return null
        }
        for (chip in filterByDateRange) {
            result = when (chip.name.lowercase()) {
                NCDFollowUpUtils.today -> Pair(startMillis, endMillis)
                NCDFollowUpUtils.tomorrow -> Pair(
                    startMillis + oneDayMillis,
                    endMillis + oneDayMillis
                )

                NCDFollowUpUtils.customise -> {
                    // Example: Provide a default custom date range or retrieve it from somewhere
                    customDate?.let {
                        Pair(
                            DateUtils.convertToTimestampWithoutZone(it.startDate, true),
                            DateUtils.convertToTimestampWithoutZone(it.endDate, false)
                        )
                    }
                }

                else -> null
            }
        }
        return result
    }

    private var sortTriple: Pair<Boolean?, String?> = Pair(null, null)

    fun sortTriple() {
        val (triple, list) = when (typeOffline) {
            NCDFollowUpUtils.SCREENED -> sortModel?.isScreeningDueDate to null
            NCDFollowUpUtils.Assessment_Type -> sortModel?.isAssessmentDueDate to null
            NCDFollowUpUtils.Defaulters_Type -> sortModel?.isMedicalReviewDueDate to null
            NCDFollowUpUtils.LTFU_Type -> {
                val isAssessmentDue = sortModel?.isAssessmentDueDate == true
                val value =
                    if (isAssessmentDue) "assessment" else if (sortModel?.isMedicalReviewDueDate == true)
                        "medicalReview" else null
                isAssessmentDue to value
            }

            else -> null to null
        }
        sortTriple = Pair(triple, list)
        searchTextOfflineLiveData.value = true
    }
}