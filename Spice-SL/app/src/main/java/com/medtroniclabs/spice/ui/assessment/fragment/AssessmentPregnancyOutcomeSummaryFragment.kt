package com.medtroniclabs.spice.ui.assessment.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentFamilyPlanningSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment responsible for showing pregnancy outcome assessment summary details
 */
@AndroidEntryPoint
class AssessmentPregnancyOutcomeSummaryFragment : BaseFragment(), View.OnClickListener {
    lateinit var binding: FragmentAssessmentFamilyPlanningSummaryBinding
    private val viewModel: AssessmentViewModel by activityViewModels()

    /**
     * Fields that need to appear in summary even though isSummary is false
     * (they are also shown in the form page, so isSummary can't be true).
     * Hidden automatically when they have no value.
     */
    private val additionalSummaryFieldIds = setOf(
        AssessmentDefinedParams.TIME_OF_DEATH,
        AssessmentDefinedParams.GESTATION_MONTH_AT_DEATH,
        AssessmentDefinedParams.CAUSE_OF_DEATH,
        AssessmentDefinedParams.GESTATION_MONTH_AT_ABORTION,
        AssessmentDefinedParams.TYPE_OF_ABORTION,
        AssessmentDefinedParams.DELIVERY_OUTCOME,
        AssessmentDefinedParams.PLACE_OF_DELIVERY,
        AssessmentDefinedParams.DATE_OF_DELIVERY,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAssessmentFamilyPlanningSummaryBinding.inflate(
            inflater,
            container,
            false,
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        attachObservers()
        viewModel.setUserJourney(AnalyticsDefinedParams.PREGNANCY_OUTCOME_SUMMARY)
    }

    private fun setListeners() {
        binding.btnDone.safeClickListener(this)
    }

    private fun attachObservers() {
        updateStatusBar()
        viewModel.assessmentStringLiveData.value?.let {
            renderSummaryData(it)
        }
    }

    private fun updateStatusBar() {
        binding.labelPhuReferred.gone()
        binding.etPhuChange.gone()
        binding.riskResultLayout.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.green_attention_color)
        binding.riskResultLayout.text = getString(R.string.no_refferral_treatment_required)
    }

    /**
     * Renders summary data
     */
    private fun renderSummaryData(data: String) {
        val convertedMap = StringConverter.stringToMap(data)
        if (convertedMap.isEmpty()) {
            showErrorInSummary()
            return
        }
        binding.emptyErrorMessage.visibility = View.GONE
        binding.parentLayout.visibility = View.VISIBLE
        binding.parentLayout.removeAllViews()

        val mapData = convertedMap[MenuConstants.PREGNANCY_OUTCOME.lowercase()] as? Map<*, *>
        if (mapData == null) {
            showErrorInSummary()
            return
        }

        // Create summary data list from form layout
        val listSummaryData = createListSummaryData(data)
        createSummaryView(listSummaryData, mapData)
    }

    private fun createListSummaryData(data: String): MutableList<AssessmentSummaryModel>? =
        viewModel.formLayoutsLiveData.value
            ?.data
            ?.formLayout
            ?.filter {
                it.isSummary == true || it.id in additionalSummaryFieldIds
            }?.map { formLayout ->
                AssessmentSummaryModel(
                    title = formLayout.titleSummary ?: formLayout.title,
                    id = formLayout.id,
                    cultureValue = formLayout.titleCulture,
                    value = AssessmentCommonUtils.getValueOfKeyFromMap(
                        StringConverter.stringToMap(data),
                        formLayout.id,
                        menuType = MenuConstants.PREGNANCY_OUTCOME.lowercase(),
                    ),
                )
            }?.toMutableList()

    private fun createSummaryView(
        listSummaryData: MutableList<AssessmentSummaryModel>?,
        mapData: Map<*, *>,
    ) {
        listSummaryData?.let { summaryData ->
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            composeSummaryView(summaryData)

            // Add newborn details sections
            addNewbornDetailsSummary(mapData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    private fun composeSummaryView(listSummaryData: MutableList<AssessmentSummaryModel>) {
        listSummaryData.forEach { item ->
            val isCounselling = item.id == AssessmentDefinedParams.COUNSELLING_ABORTION ||
                item.id == AssessmentDefinedParams.COUNSELLING_STILL_BIRTH ||
                item.id == AssessmentDefinedParams.COUNSELLING_NEONATAL_DEATH
            val isAdditionalField = item.id in additionalSummaryFieldIds

            when {
                isCounselling -> {
                    // Counselling: always show, use "-" if no value, black text
                    val displayValue = if (item.value.isNullOrBlank()) {
                        getString(R.string.seperator_hyphen)
                    } else {
                        item.value
                    }
                    bindSummaryView(item.title, displayValue, Color.BLACK)
                }
                isAdditionalField -> {
                    // Additional fields: hide if no value
                    val value = item.value
                    if (!value.isNullOrBlank()) {
                        val displayValue = if (item.id == AssessmentDefinedParams.DATE_OF_DELIVERY) {
                            DateUtils.convertDateFormat(
                                value,
                                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                                DateUtils.DATE_ddMMyyyy,
                            )
                        } else {
                            value
                        }
                        bindSummaryView(item.title, displayValue)
                    }
                }
                else -> {
                    // Other isSummary fields: show normally
                    // if (!item.value.isNullOrBlank()) {
                    bindSummaryView(item.title, item.value)
                    // }
                }
            }
        }
    }

    /**
     * Adds newborn details summary for each baby
     */
    private fun addNewbornDetailsSummary(mapData: Map<*, *>) {
        // newbornDetails could be at the top level of the pregnancy outcome map
        // or nested inside a sub-map
        val newbornDetailsList = findNewbornDetails(mapData) ?: return

        if (newbornDetailsList.isEmpty()) return

        newbornDetailsList.forEachIndexed { index, babyData ->
            if (babyData is Map<*, *>) {
                // Add baby heading
                addBabyHeading(index + 1)

                // Is baby alive
                val isBabyAlive = babyData[AssessmentDefinedParams.IS_BABY_ALIVE]
                if (isBabyAlive != null) {
                    val displayValue = when (isBabyAlive) {
                        is Boolean -> if (isBabyAlive) getString(R.string.yes) else getString(R.string.no)
                        is String -> isBabyAlive
                        else -> isBabyAlive.toString()
                    }
                    bindSummaryView(getString(R.string.is_baby_alive), displayValue)
                }

                // Sex
                val sex = babyData[AssessmentDefinedParams.SEX]
                if (sex != null) {
                    bindSummaryView(getString(R.string.sex_label), sex.toString())
                }

                // Birth weight
                val birthWeight = babyData[AssessmentDefinedParams.BIRTH_WEIGHT]
                if (birthWeight != null) {
                    bindSummaryView(getString(R.string.birth_weight_in_kg), "$birthWeight")
                }

                // Cause of neonatal death
                val causeOfDeath = babyData[AssessmentDefinedParams.CAUSE_OF_NEONATAL_DEATH]
                if (causeOfDeath != null) {
                    val displayValue = formatCauseOfDeath(causeOfDeath)
                    if (displayValue.isNotBlank()) {
                        bindSummaryView(getString(R.string.cause_of_neonatal_death), displayValue)
                    }
                }
            }
        }
    }

    private fun findNewbornDetails(mapData: Map<*, *>): List<*>? {
        // Check direct key
        val directList = mapData[AssessmentDefinedParams.NEWBORN_DETAILS]
        if (directList is List<*>) return directList

        // Check in nested maps (family groups)
        for (entry in mapData.entries) {
            if (entry.value is Map<*, *>) {
                val nestedMap = entry.value as Map<*, *>
                val nestedList = nestedMap[AssessmentDefinedParams.NEWBORN_DETAILS]
                if (nestedList is List<*>) return nestedList
            }
        }
        return null
    }

    private fun formatCauseOfDeath(value: Any?): String {
        if (value is List<*>) {
            val names = value.mapNotNull { item ->
                when (item) {
                    is Map<*, *> -> item["name"]?.toString() ?: item["symptom"]?.toString()
                    is String -> item
                    else -> null
                }
            }
            return names.joinToString(", ")
        }
        return value?.toString() ?: ""
    }

    private fun addBabyHeading(babyNumber: Int) {
        val headingView = TextView(requireContext()).apply {
            text = getString(R.string.baby_label, babyNumber)
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(requireContext().getColor(R.color.navy_blue))
            setPadding(
                resources.getDimensionPixelSize(R.dimen._16sdp),
                resources.getDimensionPixelSize(R.dimen._12sdp),
                resources.getDimensionPixelSize(R.dimen._16sdp),
                resources.getDimensionPixelSize(R.dimen._4sdp),
            )
        }
        binding.parentLayout.addView(headingView)
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.parentLayout.visibility = View.GONE
    }

    private fun bindSummaryView(
        title: String?,
        value: String?,
        valueTextColor: Int? = null,
    ) {
        value?.let { result ->
            binding.parentLayout.addView(
                AssessmentCommonUtils.addViewSummaryLayout(
                    title,
                    result,
                    valueTextColor,
                    requireContext(),
                ),
            )
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnDone.id -> {
                withLocationCheck({
                    viewModel.fetchCurrentLocation(requireContext())
                    viewModel.updatePregnantWomanAssessmentDetails()
                    viewModel.setUserJourney(AnalyticsDefinedParams.DONEBUTTONTRIGGERED)
                })
            }
        }
    }

    companion object {
        const val TAG = "AssessmentPregnancyOutcomeSummaryFragment"
    }

    fun getCurrentAnsweredStatus(): Boolean = viewModel.otherAssessmentDetails.isNotEmpty()
}
