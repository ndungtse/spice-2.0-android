package com.medtroniclabs.spice.ui.assessment.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils.convertListToString
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentAssessmentIccmSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.addViewSummaryLayout
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getListItemValue
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getValueOfKeyFromMap
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class AssessmentICCMSummaryFragment : Fragment(), View.OnClickListener {
    private val viewModel: AssessmentViewModel by activityViewModels()
    lateinit var binding: FragmentAssessmentIccmSummaryBinding
    private var datePickerDialog: DatePickerDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentIccmSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun initView() {
        binding.btnDone.safeClickListener(this)
        binding.etNotes.addTextChangedListener { input ->
            input?.let {
                val resultValue = input.trim().toString()
                if (resultValue.isNotBlank()) {
                    viewModel.otherAssessmentDetails[DefinedParams.AssessmentNotes] = resultValue
                }
            }
        }
        binding.etNextFollowUpDate.safeClickListener(this)

    }

    private fun attachObservers() {
        viewModel.assessmentSaveLiveData.value?.data?.let {
            createSummaryView(createListSummaryData(it))
        }
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.parentLayout.visibility = View.GONE
    }

    private fun createSummaryView(
        listSummaryData: MutableList<AssessmentSummaryModel>?
    ) {
        listSummaryData?.let {summaryData ->
            binding.emptyErrorMessage.visibility = View.GONE
            binding.parentLayout.visibility = View.VISIBLE
            binding.parentLayout.removeAllViews()
            composeIccmSummaryView(summaryData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    companion object {
        const val TAG = "AssessmentICCMSummaryFragment"
        fun newInstance(): AssessmentICCMSummaryFragment {
            return AssessmentICCMSummaryFragment()
        }
    }


    private fun createListSummaryData(data: String): MutableList<AssessmentSummaryModel>? {
        return viewModel.formLayout?.filter { it.isSummary == true }?.map { formLayout ->
            AssessmentSummaryModel(
                title = formLayout.title,
                id = formLayout.id,
                cultureValue = formLayout.titleCulture,
                value = getValueOfKeyFromMap(StringConverter.stringToMap(data), formLayout.id)
            )
        }?.toMutableList()
    }

    private fun composeIccmSummaryView(listSummaryData: MutableList<AssessmentSummaryModel>) {
        bindICCMSummaryView(
            getString(R.string.patient_status),
            getString(R.string.seperator_hyphen)
        )
        composeGeneralDangerSignsResult(listSummaryData)
        composeMuacResults(listSummaryData)
        composeCoughResults(listSummaryData)
        composeFeverResults(listSummaryData)
        composeDiarrhoeaResults(listSummaryData)
        composeDispensedView(listSummaryData)
    }

    private fun composeMuacResults(listSummaryData: MutableList<AssessmentSummaryModel>) {
        getListItemValue(DefinedParams.MUAC, listSummaryData)?.let {
            bindICCMSummaryView(getString(R.string.muac), it.value)
        }
    }

    private fun composeGeneralDangerSignsResult(listSummaryData: MutableList<AssessmentSummaryModel>) {
        val targetIds = setOf(
            DefinedParams.HasSleptUnusually,
            DefinedParams.HasConvulsions,
            DefinedParams.HasVomitEverything,
            DefinedParams.IsUnableToDrink
        )
        var result = DefinedParams.No
        for (assessment in listSummaryData) {
            if (assessment.id in targetIds && assessment.value == DefinedParams.Yes) {
                result = DefinedParams.Yes
            }
        }
        bindICCMSummaryView(getString(R.string.general_danger_signs), result)
    }

    private fun bindICCMSummaryView(title: String?, value: String?, valueTextColor: Int? = null) {
        binding.parentLayout.addView(
            addViewSummaryLayout(
                title,
                value,
                valueTextColor,
                requireContext()
            )
        )
    }

    private fun composeDispensedView(listSummaryData: MutableList<AssessmentSummaryModel>) {
        val dispensedList = ArrayList<String>()
        getListItemValue(DefinedParams.IsORSDispensed, listSummaryData)?.let {
            if (it.value == DefinedParams.Dispensed)
                dispensedList.add(DefinedParams.ORS)
        }
        getListItemValue(DefinedParams.IsZincDispensed, listSummaryData)?.let {
            if (it.value == DefinedParams.Dispensed)
                dispensedList.add(DefinedParams.Zinc)
        }
        getListItemValue(DefinedParams.IsACTDispensed, listSummaryData)?.let {
            if (it.value == DefinedParams.Dispensed)
                dispensedList.add(DefinedParams.ACT)
        }
        getListItemValue(DefinedParams.IsAmoxicillinDispensed, listSummaryData)?.let {
            if (it.value == DefinedParams.Dispensed)
                dispensedList.add(DefinedParams.Amoxicillin)
        }
        if (dispensedList.isNotEmpty()) {
            bindICCMSummaryView(
                getString(R.string.dispensed),
                convertListToString(dispensedList),
                getColor(requireContext(), R.color.primary_medium_blue)
            )
        }
    }

    // Utility functions to show and hide loading indicators
    private fun showLoading() {
        (activity as? BaseActivity)?.showLoading()
    }

    private fun hideLoading() {
        (activity as? BaseActivity)?.hideLoading()
    }

    private fun composeDiarrhoeaResults(listSummaryData: MutableList<AssessmentSummaryModel>) {
        getListItemValue(DefinedParams.HasDiarrhoea, listSummaryData)?.let {
            bindICCMSummaryView(
                getString(R.string.diarrhoea),
                it.value,
                if (it.value?.lowercase() == DefinedParams.Yes.lowercase()) getColor(
                    requireContext(),
                    R.color.medium_high_risk_color
                ) else null
            )
            if (it.value?.lowercase() == DefinedParams.Yes.lowercase()) {
                composeOtherDiarrhoeaMetrics(listSummaryData)
            }
        }
    }

    private fun composeOtherDiarrhoeaMetrics(listSummaryData: MutableList<AssessmentSummaryModel>) {
        getListItemValue(DefinedParams.DiarrhoeaSigns, listSummaryData)?.let {
            bindICCMSummaryView(
                getString(R.string.signs),
                it.value,
                getColor(requireContext(), R.color.medium_high_risk_color)
            )
        }
    }



    private fun composeFeverResults(listSummaryData: MutableList<AssessmentSummaryModel>) {
        getListItemValue(DefinedParams.HasFever, listSummaryData)?.let {
            val feverDisplay = if (it.value?.lowercase() == DefinedParams.Yes.lowercase()) getColor(
                requireContext(),
                R.color.medium_high_risk_color
            ) else null
            bindICCMSummaryView(getString(R.string.fever), it.value, feverDisplay)
            if (it.value?.lowercase() == DefinedParams.Yes.lowercase()) {
                binding.coughMalariaGroup.visibility = View.VISIBLE
                composeOtherFeverMetrics(listSummaryData)
            }
        }
    }

    private fun composeOtherFeverMetrics(listSummaryData: MutableList<AssessmentSummaryModel>) {
        getListItemValue(DefinedParams.RDTTestResult, listSummaryData)?.let {
            bindICCMSummaryView(
                getString(R.string.rdt_test),
                it.value,
                if (it.value?.lowercase() == DefinedParams.Positive.lowercase()) getColor(
                    requireContext(),
                    R.color.medium_high_risk_color
                ) else null
            )
        }
    }

    private fun composeCoughResults(listSummaryData: MutableList<AssessmentSummaryModel>) {
        getListItemValue(DefinedParams.HasCough, listSummaryData)?.let {
            bindICCMSummaryView(
                getString(R.string.cough),
                it.value,
                if (it.value?.lowercase() == DefinedParams.Yes.lowercase()) getColor(
                    requireContext(),
                    R.color.medium_high_risk_color
                ) else null
            )
            if (it.value?.lowercase() == DefinedParams.Yes.lowercase()) {
                binding.coughMalariaGroup.visibility = View.VISIBLE
                composeOtherCoughMetrics(listSummaryData)
            }
        }
    }

    private fun composeOtherCoughMetrics(listSummaryData: MutableList<AssessmentSummaryModel>) {
        getListItemValue(DefinedParams.NoOfBreaths, listSummaryData)?.let {
            bindICCMSummaryView(
                getString(R.string.breathing_rate),
                it.value
            )
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnDone.id -> {
                viewModel.updateOtherAssessmentDetails()
            }

            binding.etNextFollowUpDate.id -> {
                showDatePickerDialog()
            }
        }
    }


    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.etNextFollowUpDate.text.isNullOrBlank())
            yearMonthDate =
                DateUtils.convertddMMMToddMM(binding.etNextFollowUpDate.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.etNextFollowUpDate.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy
                    )
                viewModel.otherAssessmentDetails[DefinedParams.NextFollowupDate] =
                    binding.etNextFollowUpDate.text.toString()
                datePickerDialog = null
            }
        }
    }
}