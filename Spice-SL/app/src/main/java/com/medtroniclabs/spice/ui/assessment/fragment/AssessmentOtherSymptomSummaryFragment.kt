package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.Other
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentAssessmentOtherSymptomSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ACT
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Amoxicillin
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.AssessmentNotes
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ConcerningSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Dispensed
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.IsACTDispensed
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.OtherSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Positive
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.RDT
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.RDTTestResult
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.feverDays
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.feverOrHotbody
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hasFever
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.otherConcerningSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.temperature
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentOtherSymptomSummaryFragment : Fragment(), View.OnClickListener {

    lateinit var binding: FragmentAssessmentOtherSymptomSummaryBinding
    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentOtherSymptomSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        attachObservers()
    }

    private fun initViews() {
        binding.btnDone.safeClickListener(this)
        binding.etNotes.addTextChangedListener { input ->
            input?.let {
                val resultValue = input.trim().toString()
                if (resultValue.isNotBlank()) {
                    viewModel.otherAssessmentDetails[AssessmentNotes] = resultValue
                }
            }
        }
    }

    private fun attachObservers() {
        viewModel.assessmentSaveLiveData.value?.data?.let {
            createSummaryView(createListSummaryData(it))
        }
    }

    private fun createSummaryView(
        listSummaryData: MutableList<AssessmentSummaryModel>?
    ) {
        listSummaryData?.let { summaryData ->
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            renderSummaryView(summaryData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    private fun renderSummaryView(summaryData: MutableList<AssessmentSummaryModel>) {
        bindSummaryView(
            getString(R.string.patient_status),
            getString(R.string.seperator_hyphen)
        )
        renderDangerSigns(summaryData)
        summaryData.filter { it.title?.lowercase() != AssessmentDefinedParams.General_Danger_Signs.lowercase() }.forEach { item ->
            when (item.id) {
                hasFever -> {
                    if (item.value == DefinedParams.Yes) {
                        bindSummaryView(
                            item.title,
                            requireContext().getString(
                                R.string.nutrition_summary,
                                item.value,
                                getString(R.string.malaria)
                            )
                        )
                    } else {
                        bindSummaryView(item.title, item.value)
                    }
                }

                Amoxicillin.lowercase() -> {
                    if (item.value == Dispensed){
                        bindSummaryView(Dispensed, item.title)
                    }
                }

                else -> {
                        bindSummaryView(item.title, item.value)
                }
            }
        }
    }

    private fun renderDangerSigns(summaryData: MutableList<AssessmentSummaryModel>) {
        summaryData.find { it.id == ConcerningSymptoms }?.let { item ->
            val result = if (item.value == Other ) {
                summaryData.find { it.id == otherConcerningSymptoms }?.let {otherItem ->
                    requireContext().getString(R.string.other_value, item.value, otherItem.value)
                }
            } else item.value
            bindSummaryView(getString(R.string.general_danger_signs), result ?: getString(R.string.seperator_hyphen))
        }
    }

    private fun bindSummaryView(title: String?, value: String?, valueTextColor: Int? = null) {
        value?.let { result ->
            binding.parentLayout.addView(
                AssessmentCommonUtils.addViewSummaryLayout(
                    title,
                    result,
                    valueTextColor,
                    requireContext()
                )
            )
        }
    }

    private fun createListSummaryData(data: String): MutableList<AssessmentSummaryModel>? {
        return viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }?.map { formLayout ->
            AssessmentSummaryModel(
                title = formLayout.titleSummary ?: formLayout.title,
                id = formLayout.id,
                cultureValue = formLayout.titleCulture,
                value = AssessmentCommonUtils.getValueOfKeyFromMap(
                    StringConverter.stringToMap(data),
                    formLayout.id,
                    OtherSymptoms
                ),
                noOfDays = formLayout.noOfDays
            )
        }?.toMutableList()
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.parentLayout.visibility = View.GONE
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnDone.id -> {
                viewModel.updateOtherAssessmentDetails()
            }
        }
    }

}