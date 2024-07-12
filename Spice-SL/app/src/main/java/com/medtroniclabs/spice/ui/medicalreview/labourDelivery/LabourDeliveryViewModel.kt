package com.medtroniclabs.spice.ui.medicalreview.labourDelivery

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.LabourDeliveryMetaEntity
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.model.assessment.AgparScoreFooter
import com.medtroniclabs.spice.model.assessment.AgparScoreHeader
import com.medtroniclabs.spice.model.assessment.AgparScoreRow
import com.medtroniclabs.spice.model.assessment.ApgarScore
import com.medtroniclabs.spice.model.medicalreview.ApgarScoreFiveMinuteDTO
import com.medtroniclabs.spice.model.medicalreview.ApgarScoreOneMinuteDTO
import com.medtroniclabs.spice.model.medicalreview.ApgarScoreTenMinuteDTO
import com.medtroniclabs.spice.model.medicalreview.Child
import com.medtroniclabs.spice.model.medicalreview.CreateLabourDeliveryRequest
import com.medtroniclabs.spice.model.medicalreview.CreateLabourDeliveryResponse
import com.medtroniclabs.spice.model.medicalreview.LabourDTO
import com.medtroniclabs.spice.model.medicalreview.LabourDeliverySummaryDetails
import com.medtroniclabs.spice.model.medicalreview.LabourDeliverySummaryRequest
import com.medtroniclabs.spice.model.medicalreview.MotherDTO
import com.medtroniclabs.spice.model.medicalreview.NeonateDTO
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.LabourDeliveryRepository
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.enumType.AgparColumnIdentifierType
import com.medtroniclabs.spice.ui.mypatients.enumType.AgparItemViewType
import com.medtroniclabs.spice.ui.mypatients.enumType.AgparRowIdentifierType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class LabourDeliveryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: LabourDeliveryRepository
) : ViewModel() {

    val timeOfDeliveryMap = HashMap<String, Any>()
    val timeOfLabourOnsetMap = HashMap<String, Any>()
    val perineumStateMap = HashMap<String, Any>()
    var motherSignsAndSymptoms = listOf<ChipViewItemModel>()
    var motherGeneralCondition: String? = null
    var motherRiskFactors = listOf<ChipViewItemModel>()
    var motherTTDosageSoFar: String? = null
    var motherStatus = listOf<ChipViewItemModel>()
    val genderFlow = HashMap<String, Any>()
    val stateOfBaby = HashMap<String, Any>()
    val labourDeliveryMetaLiveData = MutableLiveData<Resource<Boolean>>()
    val labourDeliveryMetaList = MutableLiveData<Resource<List<LabourDeliveryMetaEntity>>>()
    val summaryDetailsLiveData = MutableLiveData<Resource<CreateLabourDeliveryRequest>>()
    val createLabourDeliveryMedicalReviewResponse =
        MutableLiveData<Resource<CreateLabourDeliveryResponse>>()
    var timeOfDeliveryInHour: String? = null
    var timeOfDeliveryInMinute: String? = null
    var timeOfLabourOnSetInHour: String? = null
    var timeOfLabourOnSetInMinutes: String? = null
    var dateOfDelivery: Triple<Int, Int, Int>? = null
    var dateOfLabourOnset: Triple<Int, Int, Int>? = null
    var deliveryType: String? = null
    var deliveryBy: String? = null
    var deliveryAt: String? = null
    var deliveryStatus: String? = null
    var noOfNeonates: String? = null
    var neonateOutcome: String? = null
    var neonateBirthWeight: String? = null
    var neonateSignsAndSymptoms = listOf<ChipViewItemModel>()
    private val _apgarScoresLiveData = MutableLiveData<List<ApgarScore>>()
    val apgarScoreLiveData: LiveData<List<ApgarScore>>
        get() = _apgarScoresLiveData

    private val _submitButtonState = MutableLiveData<Boolean>()
    val submitButtonState: LiveData<Boolean>
        get() = _submitButtonState

    var patientId: String? = null
    var lastLocation: Location? = null

    var agparColumnIdentifier: AgparColumnIdentifierType? = null
    var agparRowIdentifier: AgparRowIdentifierType? = null
    var agparSelectedScore: String? = null

    var patientDetailModel: PatientListRespModel? = null
    var isRefresh: Boolean = false
    var nextFollowupDate: String? = null
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun getAgparScoreData() {
        val apgarScores = mutableListOf<ApgarScore>()
        apgarScores.add(
            ApgarScore(
                viewType = AgparItemViewType.HEADER, AgparScoreHeader(
                    R.string.indicator_header,
                    R.string.one_minute_header,
                    R.string.five_minute_header,
                    R.string.ten_minute_header
                )
            )
        )
        apgarScores.add(
            ApgarScore(
                viewType = AgparItemViewType.ROW, row = AgparScoreRow(
                    indicatorName = R.string.activity_label,
                    indicatorType = AgparRowIdentifierType.ACTIVITY
                )
            )
        )
        apgarScores.add(
            ApgarScore(
                viewType = AgparItemViewType.ROW, row = AgparScoreRow(
                    indicatorName = R.string.pulse_label,
                    indicatorType = AgparRowIdentifierType.PULSE
                )
            )
        )
        apgarScores.add(
            ApgarScore(
                viewType = AgparItemViewType.ROW, row = AgparScoreRow(
                    indicatorName = R.string.grimace_label,
                    indicatorType = AgparRowIdentifierType.GRIMACE
                )
            )
        )
        apgarScores.add(
            ApgarScore(
                viewType = AgparItemViewType.ROW, row = AgparScoreRow(
                    indicatorName = R.string.appearance_label,
                    indicatorType = AgparRowIdentifierType.APPEARANCE
                )
            )
        )
        apgarScores.add(
            ApgarScore(
                viewType = AgparItemViewType.ROW, row = AgparScoreRow(
                    indicatorName = R.string.respiration_label,
                    indicatorType = AgparRowIdentifierType.RESPIRATION
                )
            )
        )

        apgarScores.add(
            ApgarScore(
                viewType = AgparItemViewType.FOOTER, footer = AgparScoreFooter(
                    indicatorName = R.string.total_label
                )
            )
        )

        _apgarScoresLiveData.value = apgarScores
    }

    fun updateAgparScore(
        score: String
    ) {
        _apgarScoresLiveData.value?.toMutableList()?.let { agparScores ->

            val rowPosition =
                agparScores.indexOfFirst { it.row?.indicatorType == agparRowIdentifier }

            when (agparColumnIdentifier) {
                AgparColumnIdentifierType.ONE_MINUTE -> {
                    val newRow = agparScores[rowPosition].row?.copy(oneMinute = score)
                    agparScores[rowPosition] = agparScores[rowPosition].copy(row = newRow)
                }

                AgparColumnIdentifierType.FIVE_MINUTES -> {
                    val newRow = agparScores[rowPosition].row?.copy(fiveMinute = score)
                    agparScores[rowPosition] = agparScores[rowPosition].copy(row = newRow)
                }

                else -> {
                    val newRow = agparScores[rowPosition].row?.copy(tenMinute = score)
                    agparScores[rowPosition] = agparScores[rowPosition].copy(row = newRow)
                }
            }

            var oneMinuteTotal = 0
            var fiveMinuteTotal = 0
            var tenMinuteTotal = 0
            agparScores.filter { it.viewType == AgparItemViewType.ROW }.forEach {
                it.row?.let { row ->
                    oneMinuteTotal += row.oneMinute?.toInt() ?: 0
                    fiveMinuteTotal += row.fiveMinute?.toInt() ?: 0
                    tenMinuteTotal += row.tenMinute?.toInt() ?: 0
                }
            }

            val footerPosition =
                agparScores.indexOfFirst { it.viewType == AgparItemViewType.FOOTER }

            val newFooter = agparScores[footerPosition].footer?.copy(
                oneMinuteTotal = if (oneMinuteTotal == 0) {
                    null
                } else {
                    oneMinuteTotal.toString()
                }, fiveMinuteTotal = if (fiveMinuteTotal == 0) {
                    null
                } else {
                    fiveMinuteTotal.toString()
                }, tenMinuteTotal = if (tenMinuteTotal == 0) {
                    null
                } else {
                    tenMinuteTotal.toString()
                }
            )
            agparScores[footerPosition] = agparScores[footerPosition].copy(footer = newFooter)

            _apgarScoresLiveData.value = agparScores
        }

    }

    fun getAgparRowName(): Int? {
        return when (agparRowIdentifier) {
            AgparRowIdentifierType.ACTIVITY -> R.string.activity_label
            AgparRowIdentifierType.PULSE -> R.string.pulse_label
            AgparRowIdentifierType.GRIMACE -> R.string.grimace_label
            AgparRowIdentifierType.APPEARANCE -> R.string.appearance_label
            AgparRowIdentifierType.RESPIRATION -> R.string.respiration_label
            else -> null
        }

    }

    fun getAgparColumnName(): Int? {
        return when (agparColumnIdentifier) {
            AgparColumnIdentifierType.ONE_MINUTE -> R.string.one_minute_header
            AgparColumnIdentifierType.FIVE_MINUTES -> R.string.five_minute_header
            AgparColumnIdentifierType.TEN_MINUTES -> R.string.ten_minute_header
            else -> null
        }

    }

    fun getStaticMetaData() {
        viewModelScope.launch(dispatcherIO) {
            labourDeliveryMetaLiveData.postLoading()
            labourDeliveryMetaLiveData.postValue(repository.getStaticMetaData())
        }
    }

    fun createLabourDeliveryRequest() {
        val provanceDto = ProvanceDto()
        val encounter = MedicalReviewEncounter(
            latitude = lastLocation?.latitude ?: 0.0,
            longitude = lastLocation?.longitude ?: 0.0,
            referred = true,
            startTime = DateUtils.getCurrentDateAndTime(
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
            ),
            endTime = DateUtils.getCurrentDateAndTime(
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
            ),
            householdId = patientDetailModel?.houseHoldId?.toString(),
            patientId = patientId,
            provenance = provanceDto,
            memberId = patientDetailModel?.memberId.toString()
        )
        val motherModel = createMotherModel(encounter)
        val neonateModel = createNeonateModel(encounter)
        val childModel = createChildModel(provanceDto)
        val createLabourMedicalReviewRequest = CreateLabourDeliveryRequest(
            motherDTO = motherModel,
            neonateDTO = neonateModel,
            child = childModel
        )
        viewModelScope.launch(dispatcherIO) {
            createLabourDeliveryMedicalReviewResponse.postLoading()
            createLabourDeliveryMedicalReviewResponse.postValue(
                repository.createLabourDeliveryMedicalReview(
                    request = createLabourMedicalReviewRequest
                )
            )
        }
    }

    fun getLabourDeliverySummaryDetails(
        motherId: String?,
        patientReference: String?,
        childPatientReference: String?,
        neonateId: String?
    ) {
        val request = LabourDeliverySummaryDetails(
            motherId = motherId,
            patientReference = patientReference,
            childPatientReference = childPatientReference,
            neonateId = neonateId
        )
        viewModelScope.launch(dispatcherIO) {
            summaryDetailsLiveData.postLoading()
            summaryDetailsLiveData.postValue(repository.getLabourDeliverySummaryDetails(request))
        }
    }
    private fun createChildModel(provanceDto: ProvanceDto): Child {
        val motherName = patientDetailModel?.name
        val childName = if (motherName != null) {
            "${DefinedParams.NeonateBabyNamePrefix} ${motherName}"
        } else null
        val village = patientDetailModel?.village.takeIf { it != null }
        val villageId = patientDetailModel?.villageId.takeIf { it != null }
        val childPatientId =
            "${DefinedParams.NeonatePatientIdPrefix}${DateUtils.getCurrentDateTimeInMillis()}"
        return Child(
            name = childName,
            village = village,
            villageId = villageId?.toInt(),
            motherPatientId = patientId,
            dateOfBirth = getTimeOfDelivery(),
            patientId = childPatientId,
            child = true,
            gender = "male",
            provenance = provanceDto,
            householdId = patientDetailModel?.houseHoldId.toString(),
            phoneNumber = patientDetailModel?.phoneNumber.toString(),
            householdHeadRelationship = patientDetailModel?.relationship.toString(),
            phoneNumberCategory = "personal"
        )
    }

    private fun createNeonateModel(encounter: MedicalReviewEncounter): NeonateDTO {
        val apgarScoreOneMinute = createOneMinuteApgarScore()
        val apgarScoreFiveMinute = createFiveMinuteApgarScore()
        val apgarScoreTenMinute = createTenMinuteApgarScore()
        return NeonateDTO(neonateOutcome = neonateOutcome.takeIf { it != null },
            gender = "male",
            birthWeight = neonateBirthWeight.takeIf { it != null },
            stateOfBaby = stateOfBaby[DefinedParams.StateOfBaby] as? String,
            signs = neonateSignsAndSymptoms.map { it.name }.takeIf { it.isNotEmpty() },
            encounter = encounter,
            apgarScoreOneMinuteDTO = apgarScoreOneMinute.takeIf { it != null },
            apgarScoreFiveMinuteDTO = apgarScoreFiveMinute.takeIf { it != null },
            apgarScoreTenMinuteDTO = apgarScoreTenMinute.takeIf { it != null }
        )
    }

    private fun createTenMinuteApgarScore(): ApgarScoreTenMinuteDTO? {
        val apgarScores = _apgarScoresLiveData.value ?: return null
        val tenMinuteScores =
            apgarScores.filter { it.viewType == AgparItemViewType.ROW }.map { it.row?.tenMinute }
        val tenMinuteTotal = apgarScores.filter { it.viewType == AgparItemViewType.FOOTER }
            .map { it.footer?.tenMinuteTotal }

        if (tenMinuteScores.isEmpty() || tenMinuteScores.all { it == null }) {
            return null
        }

        return ApgarScoreTenMinuteDTO(
                activity = tenMinuteScores[0]?.toInt(),
                pulse = tenMinuteScores[1]?.toInt(),
                grimace = tenMinuteScores[2]?.toInt(),
                appearance = tenMinuteScores[3]?.toInt(),
                respiration = tenMinuteScores[4]?.toInt(),
                tenMinuteTotalScore = tenMinuteTotal[0]?.toInt()
            )
    }

    private fun createFiveMinuteApgarScore(): ApgarScoreFiveMinuteDTO? {
        val apgarScores = _apgarScoresLiveData.value ?: return null
        val fiveMinuteScores =
            apgarScores.filter { it.viewType == AgparItemViewType.ROW }.map { it.row?.fiveMinute }
        val fiveMinuteTotal = apgarScores.filter { it.viewType == AgparItemViewType.FOOTER }
            .map { it.footer?.fiveMinuteTotal }

        if (fiveMinuteScores.isEmpty() || fiveMinuteScores.all { it == null }) {
            return null
        }

        return ApgarScoreFiveMinuteDTO(
                activity = fiveMinuteScores[0]?.toInt(),
                pulse = fiveMinuteScores[1]?.toInt(),
                grimace = fiveMinuteScores[2]?.toInt(),
                appearance = fiveMinuteScores[3]?.toInt(),
                respiration = fiveMinuteScores[4]?.toInt(),
                fiveMinuteTotalScore = fiveMinuteTotal[0]?.toInt()
            )
    }

    private fun createOneMinuteApgarScore(): ApgarScoreOneMinuteDTO? {
        val apgarScores = _apgarScoresLiveData.value ?: return null
        val oneMinuteScores =
            apgarScores.filter { it.viewType == AgparItemViewType.ROW }.map { it.row?.oneMinute }
        val oneMinuteTotal = apgarScores.filter { it.viewType == AgparItemViewType.FOOTER }
            .map { it.footer?.oneMinuteTotal }

        if (oneMinuteScores.isEmpty() || oneMinuteScores.all { it == null }) {
            return null
        }

        return ApgarScoreOneMinuteDTO(
                activity = oneMinuteScores[0]?.toInt(),
                pulse = oneMinuteScores[1]?.toInt(),
                grimace = oneMinuteScores[2]?.toInt(),
                appearance = oneMinuteScores[3]?.toInt(),
                respiration = oneMinuteScores[4]?.toInt(),
                oneMinuteTotalScore = oneMinuteTotal[0]?.toInt()
            )
    }

    private fun createMotherModel(encounter: MedicalReviewEncounter): MotherDTO {
        val motherTTDosage = motherTTDosageSoFar
        val labourDTO = createLabourDeliveryModel()
        return MotherDTO(signs = motherSignsAndSymptoms.map { it.name }.takeIf { it.isNotEmpty() },
            generalConditions = motherGeneralCondition.takeIf { it != null },
            riskFactors = motherRiskFactors.map { it.name }.takeIf { it.isNotEmpty() },
            stateOfPerineum = perineumStateMap[DefinedParams.StateOfPerineum] as? String,
            ttDoseTaken = if (motherTTDosage.isNullOrEmpty()) null else motherTTDosage.toInt(),
            status = motherStatus.map { it.name }.takeIf { it.isNotEmpty() },
            tear = perineumStateMap[DefinedParams.Tear] as? String,
            encounter = encounter,
            labourDTO = labourDTO
        )
    }

    private fun createLabourDeliveryModel(): LabourDTO {
        return LabourDTO(
            deliveryAt = deliveryAt,
            deliveryBy = deliveryBy,
            deliveryStatus = deliveryStatus,
            deliveryType = deliveryType,
            noOfNeoNates = noOfNeonates?.toInt(),
            dateAndTimeOfDelivery = getTimeOfDelivery(),
            dateAndTimeOfLabourOnset = getTimeOfLabourOnset()
        )
    }

    private fun getTimeOfDelivery(): String? {
        val dateOfDelivery = this.dateOfDelivery ?: return null
        val timeOfDeliveryInHourInt = timeOfDeliveryInHour?.toInt() ?: 0
        val timeOfDeliveryInMinutesInt = timeOfDeliveryInMinute?.toInt() ?: 0
        val timeOfDeliveryMap = this.timeOfDeliveryMap[DefinedParams.TimeOfDelivery] as String
        val localTimeWithAMPM = "${
            DateTimeFormatter.ofPattern(DateUtils.TIME_FORMAT_hhmm)
                .format(LocalTime.of(timeOfDeliveryInHourInt, timeOfDeliveryInMinutesInt))
        } $timeOfDeliveryMap"
        val localTime = LocalTime.parse(
            localTimeWithAMPM,
            DateTimeFormatter.ofPattern(DateUtils.TIME_FORMAT_hhmma)
        )
        val localDate =
            LocalDate.of(dateOfDelivery.first, dateOfDelivery.second, dateOfDelivery.third)
        val localDateTime = LocalDateTime.of(localDate, localTime)
        return DateTimeFormatter.ofPattern(DateUtils.DATE_FORMAT_yyyyMMddHHmmss)
            .format(localDateTime)
    }

    private fun getTimeOfLabourOnset(): String? {
        val dateOfLabourOnset = this.dateOfLabourOnset ?: return null
        val timeOfLabourOnSetInHourInt = timeOfLabourOnSetInHour?.toInt() ?: 0
        val timeOfLabourOnSetInMinuteInt = timeOfLabourOnSetInMinutes?.toInt() ?: 0
        val timeOfLabourOnSetMap =
            this.timeOfLabourOnsetMap[DefinedParams.TimeOfLabourOnset] as String
        val localTimeWithAMPM = "${
            DateTimeFormatter.ofPattern(DateUtils.TIME_FORMAT_hhmm)
                .format(LocalTime.of(timeOfLabourOnSetInHourInt, timeOfLabourOnSetInMinuteInt))
        } $timeOfLabourOnSetMap"
        val localTime = LocalTime.parse(
            localTimeWithAMPM,
            DateTimeFormatter.ofPattern(DateUtils.TIME_FORMAT_hhmma)
        )
        val localDate =
            LocalDate.of(dateOfLabourOnset.first, dateOfLabourOnset.second, dateOfLabourOnset.third)
        val localDateTime = LocalDateTime.of(localDate, localTime)
        return DateTimeFormatter.ofPattern(DateUtils.DATE_FORMAT_yyyyMMddHHmmss)
            .format(localDateTime)
    }


    fun getLabourDeliveryMetaList() {
        viewModelScope.launch(dispatcherIO) {
            labourDeliveryMetaList.postLoading()
            labourDeliveryMetaList.postValue(repository.getLabourDeliveryList())
        }
    }

    fun validateSubmitButtonState() {
        val apgarScore = _apgarScoresLiveData.value
            ?.filter { it.viewType == AgparItemViewType.ROW }
            ?.any { it.row?.oneMinute != null || it.row?.fiveMinute != null || it.row?.tenMinute != null }
        _submitButtonState.value = (timeOfDeliveryMap.isNotEmpty()
                || timeOfLabourOnsetMap.isNotEmpty()
                || dateOfDelivery != null
                || dateOfLabourOnset != null
                || timeOfDeliveryInHour != null
                || timeOfDeliveryInMinute != null
                || timeOfLabourOnSetInHour != null
                || timeOfLabourOnSetInMinutes != null
                || deliveryType != null
                || deliveryBy != null
                || deliveryAt != null
                || deliveryStatus != null
                || noOfNeonates != null
                || motherSignsAndSymptoms.isNotEmpty()
                || motherGeneralCondition != null
                || motherRiskFactors.isNotEmpty()
                || perineumStateMap.isNotEmpty()
                || motherTTDosageSoFar != null
                || motherStatus.isNotEmpty()
                || neonateOutcome != null
                || genderFlow.isNotEmpty()
                || neonateBirthWeight != null
                || stateOfBaby.isNotEmpty()
                || neonateSignsAndSymptoms.isNotEmpty()
                || apgarScore == true)

    }

    fun labourDeliverySummaryCreate(
        patientReference: String,
        motherId: String,
        memberId: String?
    ) {
        val request = LabourDeliverySummaryRequest(
            id = motherId,
            nextVisitDate = DateUtils.convertDateTimeToDate(
                nextFollowupDate,
                DateUtils.DATE_ddMMyyyy,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                inUTC = true
            ),
            memberId = memberId,
            patientStatus = "postal",
            patientReference = patientReference,
            provenance = ProvanceDto(),
            referralTicketType = MedicalReviewTypeEnums.RMNCH.name
        )
        viewModelScope.launch(dispatcherIO) {
            summaryCreateResponse.postLoading()
            summaryCreateResponse.postValue(repository.labourDeliverySummaryCreate(request))
        }
    }
}