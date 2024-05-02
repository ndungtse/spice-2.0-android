package com.medtroniclabs.spice.ui.medicalreview.labourDelivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.LabourDeliveryMetaEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.assessment.AgparScore
import com.medtroniclabs.spice.model.assessment.AgparScoreFooter
import com.medtroniclabs.spice.model.assessment.AgparScoreHeader
import com.medtroniclabs.spice.model.assessment.AgparScoreRow
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.LabourDeliveryRepository
import com.medtroniclabs.spice.ui.mypatients.enumType.AgparColumnIdentifierType
import com.medtroniclabs.spice.ui.mypatients.enumType.AgparItemViewType
import com.medtroniclabs.spice.ui.mypatients.enumType.AgparRowIdentifierType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LabourDeliveryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: LabourDeliveryRepository
) : ViewModel() {

    val timeOfDeliveryMap = HashMap<String, Any>()
    val timeOfLabourOnsetMap = HashMap<String, Any>()
    val perineumStateMap = HashMap<String, Any>()
    val genderFlow = HashMap<String, Any>()
    val stateOfBaby = HashMap<String, Any>()
    val labourDeliveryMetaLiveData = MutableLiveData<Resource<Boolean>>()
    val labourDeliveryMetaList = MutableLiveData<Resource<List<LabourDeliveryMetaEntity>>>()
    private val _agparScoresLiveData = MutableLiveData<List<AgparScore>>()
    val agparScoreLiveData: LiveData<List<AgparScore>>
        get() = _agparScoresLiveData

    var agparColumnIdentifier: AgparColumnIdentifierType? = null
    var agparRowIdentifier: AgparRowIdentifierType? = null
    var agparSelectedScore: String? = null

    fun getAgparScoreData() {
        val agparScores = mutableListOf<AgparScore>()
        agparScores.add(
            AgparScore(
                viewType = AgparItemViewType.HEADER,
                AgparScoreHeader(
                    R.string.indicator_header,
                    R.string.one_minute_header,
                    R.string.five_minute_header,
                    R.string.ten_minute_header
                )
            )
        )
        agparScores.add(
            AgparScore(
                viewType = AgparItemViewType.ROW, row = AgparScoreRow(
                    indicatorName = R.string.activity_label,
                    indicatorType = AgparRowIdentifierType.ACTIVITY
                )
            )
        )
        agparScores.add(
            AgparScore(
                viewType = AgparItemViewType.ROW, row = AgparScoreRow(
                    indicatorName = R.string.pulse_label,
                    indicatorType = AgparRowIdentifierType.PULSE
                )
            )
        )
        agparScores.add(
            AgparScore(
                viewType = AgparItemViewType.ROW, row = AgparScoreRow(
                    indicatorName = R.string.grimace_label,
                    indicatorType = AgparRowIdentifierType.GRIMACE
                )
            )
        )
        agparScores.add(
            AgparScore(
                viewType = AgparItemViewType.ROW, row = AgparScoreRow(
                    indicatorName = R.string.appearance_label,
                    indicatorType = AgparRowIdentifierType.APPEARANCE
                )
            )
        )
        agparScores.add(
            AgparScore(
                viewType = AgparItemViewType.ROW, row = AgparScoreRow(
                    indicatorName = R.string.respiration_label,
                    indicatorType = AgparRowIdentifierType.RESPIRATION
                )
            )
        )

        agparScores.add(
            AgparScore(
                viewType = AgparItemViewType.FOOTER, footer = AgparScoreFooter(
                    indicatorName = R.string.total_label
                )
            )
        )

        _agparScoresLiveData.value = agparScores
    }

    fun updateAgparScore(
        score: String
    ) {
        _agparScoresLiveData.value?.toMutableList()?.let { agparScores ->

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
                },
                fiveMinuteTotal = if (fiveMinuteTotal == 0) {
                    null
                } else {
                    fiveMinuteTotal.toString()
                },
                tenMinuteTotal = if (tenMinuteTotal == 0) {
                    null
                } else {
                    tenMinuteTotal.toString()
                }
            )
            agparScores[footerPosition] = agparScores[footerPosition].copy(footer = newFooter)

            _agparScoresLiveData.value = agparScores
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

    fun getLabourDeliveryMetaList() {
        viewModelScope.launch(dispatcherIO) {
            labourDeliveryMetaList.postLoading()
            labourDeliveryMetaList.postValue(repository.getLabourDeliveryList())
        }
    }
}