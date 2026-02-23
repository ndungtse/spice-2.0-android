package com.medtroniclabs.spice.ui.medicalreview.epi.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.getLocalDate
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.EpiGroupName
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.medicalreview.EpiCatchUpPolicyItem
import com.medtroniclabs.spice.model.medicalreview.EpiNextVaccinationDetails
import com.medtroniclabs.spice.model.medicalreview.RequestCreateImmunisation
import com.medtroniclabs.spice.model.medicalreview.RequestImmunisationSummaryCreate
import com.medtroniclabs.spice.model.medicalreview.RequestImmunisationSummaryDetail
import com.medtroniclabs.spice.model.medicalreview.RequestVaccinationList
import com.medtroniclabs.spice.model.medicalreview.ResponseCreateImmunisation
import com.medtroniclabs.spice.model.medicalreview.ResponseImmunisationSummaryCreate
import com.medtroniclabs.spice.model.medicalreview.ResponseImmunisationSummaryDetails
import com.medtroniclabs.spice.model.medicalreview.VaccinationDetail
import com.medtroniclabs.spice.model.medicalreview.VaccinationGroupItem
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.ImmunisationRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ImmunisationViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val immunisationRepository: ImmunisationRepository,
) : BaseViewModel(dispatcherIO) {
    var encounterId: String = ""
    var patientReferenceId: String = ""

    val immunisationDetailListLiveData = MutableLiveData<Resource<List<VaccinationGroupItem>>>()
    val showMissedVaccinationDialog = MutableLiveData<Pair<Boolean, String?>>()
    val addedVaccinationItemLiveData = MutableLiveData<VaccinationDetail>()
    val changesList = mutableListOf<VaccinationDetail>()

    val saveImmunisationListLiveData = MutableLiveData<Resource<ResponseCreateImmunisation>>()
    val immunisationSummaryLiveData = MutableLiveData<Resource<ResponseImmunisationSummaryDetails>>()
    var nextVaccinationDetails: EpiNextVaccinationDetails? = null
    var nextVisitDate: String? = null
    val updateScheduleDateAndVaccinationDate = MutableLiveData<Pair<LocalDate, LocalDate>>()
    val saveImmunisationSummaryLiveData = MutableLiveData<Resource<ResponseImmunisationSummaryCreate>>()
    val shouldRefreshListLiveData = MutableLiveData<Boolean>()

    val epiCatchUpPolicyItems = MutableLiveData<Resource<List<EpiCatchUpPolicyItem>>>()
    private val dayDelay = "Day Delay"
    private val daysDelay = "Days Delay"
    private val onTime = "On-Time"

    fun getImmunisationDetails(
        id: String?,
        memberId: String?,
        patientId: String?,
        dob: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            val request = RequestVaccinationList(id, memberId, patientId, dob)
            immunisationRepository.getImmunisationDetails(request, immunisationDetailListLiveData)
        }
    }

    fun updateImmunisationDetails(list: List<VaccinationGroupItem>) {
        var takenAnyOneLastSchedule = true
        val historyMap = mutableMapOf<String, LocalDate?>()
        list.forEach { group ->
            group.vaccinationItems.forEach { item ->
                if (takenAnyOneLastSchedule) {
                    val lastVaccinatedDate = historyMap[item.category]
                    val scheduledDate = item.scheduledDate.getLocalDate()

                    item.updatedScheduleDate = when {
                        item.vaccineOrder == 1 -> scheduledDate
                        lastVaccinatedDate != null && lastVaccinatedDate.isBefore(scheduledDate) -> scheduledDate
                        else -> lastVaccinatedDate
                    }

                    historyMap[item.category] = item.vaccinatedDate?.getLocalDate()?.plusWeeks(4)
                } else {
                    item.updatedScheduleDate = null
                    historyMap[item.category] = null
                }
            }
            takenAnyOneLastSchedule = group.vaccinationItems.any { it.vaccinatedDate != null }
        }

        shouldRefreshListLiveData.postValue(true)
    }

    fun getEpiCatchUpPolicyItems() {
        viewModelScope.launch(dispatcherIO) {
            immunisationRepository.getCatchUpPolicyItems(epiCatchUpPolicyItems)
        }
    }

    fun shouldShowMissedVaccinationDialog(
        flag: Boolean,
        reason: String?,
    ) {
        showMissedVaccinationDialog.postValue(Pair(flag, reason))
    }

    fun addVaccinationDetail(vaccinationItem: VaccinationDetail) {
        addedVaccinationItemLiveData.postValue(vaccinationItem)
    }

    fun getMissedVaccineCount(): Int {
        var missedCount = 0
        val vaccinationList = immunisationDetailListLiveData.value?.data ?: listOf()
        val ldToday = LocalDate.now()
        for (item in vaccinationList) {
            val scheduleDate = item.scheduleDate.getLocalDate()
            if (scheduleDate.isAfter(ldToday)) {
                break
            } else {
                item.vaccinationItems.forEach { vaccine ->
                    if (vaccine.updatedScheduleDate != null && !vaccine.updatedScheduleDate!!.isAfter(ldToday) && vaccine.vaccinatedDate == null) {
                        missedCount += 1
                    }
                }
            }
        }

        return missedCount
    }

    private fun hasAnyPendingVaccine(): Boolean {
        val vaccinationList = immunisationDetailListLiveData.value?.data ?: listOf()
        val allVaccine = vaccinationList.flatMap { it.vaccinationItems }
        return allVaccine.any { it.vaccinatedDate == null }
    }

    fun getLastVaccineScheduleDateAndVaccinationDate() {
        val vaccinationList = immunisationDetailListLiveData.value?.data ?: listOf()
        var lastScheduleDate: LocalDate? = null
        var lastVaccinatedDate: LocalDate? = null
        var maxDelay = -1L
        for (item in vaccinationList) {
            val vaccinatedItems = item.vaccinationItems.filter { it.vaccinatedDate != null }
            if (vaccinatedItems.isEmpty()) {
                break
            }

            lastScheduleDate = null
            lastVaccinatedDate = null
            vaccinatedItems.forEach { vaccine ->
                if (vaccine.vaccinatedDate != null && vaccine.updatedScheduleDate != null) {
                    val vaccinatedDate = vaccine.vaccinatedDate!!.getLocalDate()
                    val scheduledDate = vaccine.updatedScheduleDate
                    val delay = ChronoUnit.DAYS.between(scheduledDate, vaccinatedDate)
                    if (delay > maxDelay) {
                        lastScheduleDate = scheduledDate
                        lastVaccinatedDate = vaccinatedDate
                        maxDelay = delay
                    }
                }
            }
            maxDelay = -1L // Reset for Next Group
        }

        lastScheduleDate?.let { sDate ->
            lastVaccinatedDate?.let { vDate ->
                updateScheduleDateAndVaccinationDate.postValue(Pair(sDate, vDate))
            }
        }
    }

    fun computeAnyMissedSummary() {
        var containsAnyMissedVaccine = false
        changesList.clear()
        val vaccinationList = immunisationDetailListLiveData.value?.data ?: listOf()
        val ldToday = LocalDate.now()
        for (item in vaccinationList) {
            val scheduleDate = item.scheduleDate.getLocalDate()
            if (scheduleDate.isAfter(ldToday)) { // After - Upcoming
                val first = item.vaccinationItems.first()
                val nextDoses =
                    item.vaccinationItems.map { it.vaccineName }.toList()
                val nextEpi = EpiNextVaccinationDetails(
                    EpiGroupName.getGroupName(first.value, first.type),
                    nextDoses,
                    item.scheduleDate,
                )
                nextVaccinationDetails = nextEpi
                break
            } else {
                item.vaccinationItems.forEach { vaccine ->
                    if (vaccine.updatedScheduleDate != null) {
                        if (vaccine.isEdited == true) {
                            changesList.add(vaccine.copy())
                        } else if (!vaccine.updatedScheduleDate!!.isAfter(ldToday) &&
                            vaccine.vaccinatedDate == null
                        ) {
                            containsAnyMissedVaccine = true
                            changesList.add(vaccine.copy(status = "Missed"))
                        }
                    }
                }
            }
        }

        if (nextVaccinationDetails != null) {
            nextVisitDate = nextVaccinationDetails!!.nextVisitDate
        } else {
            if (hasAnyPendingVaccine()) {
                val tomorrowDate = LocalDate
                    .now()
                    .plusDays(1)
                    .format(DateTimeFormatter.ofPattern(DateUtils.DATE_ddMMyyyy))
                val stringTomorrowDate = DateUtils.convertDateTimeToDate(
                    tomorrowDate,
                    DateUtils.DATE_ddMMyyyy,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    inUTC = true,
                )
                nextVisitDate = stringTomorrowDate
            }
        }

        showMissedVaccinationDialog.postValue(Pair(containsAnyMissedVaccine, null))
    }

    fun postVaccinationChanges(
        id: String?,
        memberId: String?,
        patientId: String?,
        missedReason: String?,
        householdId: String?,
        villageId: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            val request = RequestCreateImmunisation(
                immunisationList = changesList,
                encounter = MedicalReviewEncounter(
                    patientReference = id,
                    patientId = patientId,
                    memberId = memberId,
                    provenance = ProvanceDto(),
                    latitude = SecuredPreference.getDouble(SecuredPreference.EnvironmentKey.CURRENT_LATITUDE.name),
                    longitude = SecuredPreference.getDouble(SecuredPreference.EnvironmentKey.CURRENT_LONGITUDE.name),
                    villageId = villageId,
                    householdId = householdId,
                ),
                missedReason = missedReason,
            )

            immunisationRepository.saveImmunisationList(request, saveImmunisationListLiveData)
        }
    }

    fun getImmunisationSummaryDetails(encounterId: String) {
        viewModelScope.launch(dispatcherIO) {
            val request = RequestImmunisationSummaryDetail(encounterId)
            immunisationRepository.getImmunisationSummaryDetails(request, immunisationSummaryLiveData)
        }
    }

    fun saveImmunisationSummaryDetails(
        encounterId: String,
        id: String?,
        memberId: String?,
        patientId: String?,
        villageId: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            immunisationSummaryLiveData.value?.data?.let { summaryDetails ->
                val lastScheduleDetail = getLastScheduleReason()
                val request = RequestImmunisationSummaryCreate(
                    vaccinated = summaryDetails.vaccinated,
                    missedVaccine = summaryDetails.missedVaccine,
                    missedReason = summaryDetails.missedReason,
                    lastScheduledDate = lastScheduleDetail.first ?: "",
                    lastScheduledDateReason = lastScheduleDetail.second ?: "",
                    encounterId = encounterId,
                    memberId = memberId,
                    patientId = patientId,
                    nextVaccinationDuration = nextVaccinationDetails?.nextVaccinationDuration,
                    nextVaccinationDose = nextVaccinationDetails?.nextVaccinationDose,
                    nextVaccinationDate = nextVisitDate,
                    provenance = ProvanceDto(),
                    villageId = villageId,
                    patientReference = id,
                )
                immunisationRepository.saveImmunisationSummaryDetails(request, saveImmunisationSummaryLiveData)
            }
        }
    }

    private fun getLastScheduleReason(): Pair<String?, String?> {
        updateScheduleDateAndVaccinationDate.value?.let { pair ->
            val dayDiff = ChronoUnit.DAYS.between(pair.first, pair.second)
            val scheduleDate = pair.first.format(DateTimeFormatter.ofPattern(DATE_ddMMyyyy))

            val status = when {
                dayDiff > 1L -> "$dayDiff $daysDelay"
                dayDiff == 1L -> "$dayDiff $dayDelay"
                else -> onTime
            }

            val lasScheduleDate = DateUtils.convertDateTimeToDate(
                scheduleDate,
                DateUtils.DATE_ddMMyyyy,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                inUTC = true,
            )

            return Pair(lasScheduleDate, status)
        }

        return Pair(null, null)
    }
}
