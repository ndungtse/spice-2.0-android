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
        summaryData.forEach { item ->
            if (item.id == Amoxicillin && item.value == Dispensed) {
                bindSummaryView(Dispensed, item.title)
            } else {
                bindSummaryView(item.title, item.value)
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

    private fun renderFeverResults(listSummaryData: MutableList<AssessmentSummaryModel>) {
        AssessmentCommonUtils.getListItemValue(feverOrHotbody, listSummaryData)
            ?.let { result ->
                val feverDisplay =
                    if (result.value?.lowercase() == DefinedParams.Yes.lowercase()) ContextCompat.getColor(
                        requireContext(),
                        R.color.medium_high_risk_color
                    ) else null
                bindSummaryView(
                    getString(R.string.fever),
                    result.value ?: getString(R.string.seperator_hyphen),
                    feverDisplay
                )
                if (result.value?.lowercase() == DefinedParams.Yes.lowercase()) {
                    listSummaryData.find { it.id == temperature }?.let { item ->
                        bindSummaryView(
                            item.title?.capitalizeFirstChar(),
                            item.value ?: getString(R.string.seperator_hyphen)
                        )
                    }
                    listSummaryData.find { it.id == feverDays }?.let { item ->
                        bindSummaryView(
                            item.title,
                            item.value ?: getString(R.string.seperator_hyphen)
                        )
                    }
                listSummaryData.find { it.id == RDTTestResult }?.let { item ->
                        bindSummaryView(RDT, item.value ?: getString(R.string.seperator_hyphen))
                    }
                    listSummaryData.find { it.id == IsACTDispensed }?.let { item ->
                        bindSummaryView(
                            getString(R.string.dispensed),
                            if (item.value == Dispensed) ACT else getString(R.string.hyphen_symbol)
                        )
                    }
                    renderOtherFeverMetrics(listSummaryData)
                }
            }
    }

    private fun renderOtherFeverMetrics(listSummaryData: MutableList<AssessmentSummaryModel>) {
        AssessmentCommonUtils.getListItemValue(RDTTestResult, listSummaryData)?.let {
            bindSummaryView(
                getString(R.string.rdt_test),
                it.value,
                if (it.value?.lowercase() == Positive.lowercase()) ContextCompat.getColor(
                    requireContext(),
                    R.color.medium_high_risk_color
                ) else null
            )
        }
    }

    private fun bindSummaryView(title: String?, value: String?, valueTextColor: Int? = null) {
        binding.parentLayout.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title,
                value,
                valueTextColor,
                requireContext()
            )
        )
    }

    private fun createListSummaryData(data: String): MutableList<AssessmentSummaryModel>? {
        return viewModel.formLayout?.filter { it.isSummary == true }?.map { formLayout ->
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