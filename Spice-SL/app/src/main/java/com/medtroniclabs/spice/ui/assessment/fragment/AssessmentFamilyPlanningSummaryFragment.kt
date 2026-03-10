package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.familyPlanning
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentFamilyPlanningSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class AssessmentFamilyPlanningSummaryFragment : BaseFragment(), View.OnClickListener {
    private val viewModel: AssessmentViewModel by activityViewModels()
    lateinit var binding: FragmentAssessmentFamilyPlanningSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAssessmentFamilyPlanningSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.assessmentStringLiveData.value?.let {
            updateStatusBar()
            createSummaryView(createListSummaryData(it))
        }
    }

    private fun initView() {
        viewModel.setUserJourney(AnalyticsDefinedParams.FAMILYPLANNINGSUMMARY)
        binding.btnDone.safeClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnDone -> {
                viewModel.setUserJourney(AnalyticsDefinedParams.DONEBUTTONTRIGGERED)
                viewModel.updateOtherAssessmentDetails()
            }
        }
    }

    private fun createListSummaryData(data: String): MutableList<AssessmentSummaryModel>? =
        viewModel.formLayoutsLiveData.value
            ?.data
            ?.formLayout
            ?.filter {
                it.isSummary == true
            }?.map { formLayout ->
                AssessmentSummaryModel(
                    title = formLayout.titleSummary ?: formLayout.title,
                    id = formLayout.id,
                    cultureValue = formLayout.titleCulture,
                    value = AssessmentCommonUtils.getValueOfKeyFromMap(
                        StringConverter.stringToMap(data),
                        formLayout.id,
                        menuType = familyPlanning.lowercase(),
                    ),
                )
            }?.toMutableList()

    private fun updateStatusBar() {
        // Always hide PHU fields
        binding.labelPhuReferred.gone()
        binding.etPhuChange.gone()

        binding.riskResultLayout.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.green_attention_color)
        binding.riskResultLayout.text = getString(R.string.no_refferral_treatment_required)
    }

    private fun createSummaryView(listSummaryData: MutableList<AssessmentSummaryModel>?) {
        listSummaryData?.let { summaryData ->
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            composeSummaryView(summaryData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    /**
     * Extracts raw value directly from assessment data for family planning methods
     * This handles the case where getValueOfKeyFromMap returns empty for ArrayList<String>
     */
    private fun getRawFamilyPlanningMethodValue(): Any? =
        viewModel.assessmentStringLiveData.value?.let { data ->
            val map = StringConverter.stringToMap(data)
            val familyPlanningMap = map[familyPlanning.lowercase()] as? Map<*, *>
            val clientProfileMap = familyPlanningMap?.get(AssessmentDefinedParams.FamilyPlanningDetails) as? Map<*, *>
            clientProfileMap?.get(AssessmentDefinedParams.FamilyPlanningMethods)
        }

    private fun composeSummaryView(listSummaryData: MutableList<AssessmentSummaryModel>) {
        listSummaryData.forEach { item ->
            val fieldId = item.id
            when (fieldId) {
                AssessmentDefinedParams.FamilyPlanningMethods -> {
                    // Convert spinner ID to display name
                    // Handle case where getValueOfKeyFromMap returns empty for ArrayList<String>
                    val processedValue = item.value
                    val rawValue = if (processedValue.isNullOrBlank()) {
                        getRawFamilyPlanningMethodValue()
                    } else {
                        processedValue
                    }

                    val valueId = when (rawValue) {
                        is String -> rawValue
                        is List<*> -> {
                            // Handle ArrayList<String> format - extract first string element
                            (rawValue.firstOrNull() as? String) ?: ""
                        }
                        else -> rawValue?.toString() ?: ""
                    }

                    if (valueId.isNotBlank()) {
                        val displayValue = getSpinnerDisplayValue(fieldId, valueId)
                        bindSummaryView(item.title, displayValue)
                    }
                }
                AssessmentDefinedParams.DesireForChildrenInFuture -> {
                    // Convert spinner ID to display name
                    item.value?.let { value ->
                        val displayValue = getSpinnerDisplayValue(fieldId, value)
                        bindSummaryView(item.title, displayValue)
                    }
                }
                AssessmentDefinedParams.SpecifySideEffects, AssessmentDefinedParams.CondomsStatus, AssessmentDefinedParams.Contraceptive, AssessmentDefinedParams.MemberUsingAnyFamilyPlanning, AssessmentDefinedParams.NeedOfOtherFamilyPlanning, AssessmentDefinedParams.IsAnySideEffects -> {
                    item.value?.let {
                        bindSummaryView(item.title, it)
                    }
                }
                else -> {
                    // Handle other fields with values
                    item.value?.let {
                        bindSummaryView(item.title, it)
                    }
                }
            }
        }
    }

    /**
     * Converts spinner field ID to display name using optionsList from formLayout
     */
    private fun getSpinnerDisplayValue(
        fieldId: String,
        valueId: String,
    ): String {
        val formLayout = viewModel.formLayoutsLiveData.value?.data?.formLayout?.find {
            it.id == fieldId
        } ?: return valueId

        val optionsList = formLayout.optionsList ?: return valueId
        optionsList.forEach { option ->
            val optionId = option[DefinedParams.id] as? String
            if (optionId == valueId) {
                return option[DefinedParams.NAME] as? String ?: valueId
            }
        }

        return valueId
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
        binding.parentLayout.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title,
                value,
                valueTextColor,
                requireContext(),
            ),
        )
    }

    companion object {
        const val TAG = "AssessmentFamilyPlanningSummaryFragment"

        fun newInstance(): AssessmentFamilyPlanningSummaryFragment = AssessmentFamilyPlanningSummaryFragment()
    }

    fun getCurrentAnsweredStatus(): Boolean = viewModel.otherAssessmentDetails.isNotEmpty()
}
