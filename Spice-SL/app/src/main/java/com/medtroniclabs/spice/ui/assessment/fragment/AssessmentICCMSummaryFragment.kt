package com.medtroniclabs.spice.ui.assessment.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils.getOptionMap
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.ICCM
import com.medtroniclabs.spice.common.DefinedParams.Yes
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentAssessmentIccmSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.addViewSummaryLayout
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getNutritionStatus
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getValueOfKeyFromMap
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ACT
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Amoxicillin
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.AssessmentNotes
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.BreathPerMinute
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Dispensed
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.General_Danger_Signs
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.IsClinicTaken
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NextFollowupDate
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NoOfDaysDiarrhoea
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NoOfDaysOfCough
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.NoOfDaysOfFever
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.OrsDispensedStatus
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.ZincDispensedStatus
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hasCough
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.hasFever
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isBreastfeed
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isConvulsionPastFewDays
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isDiarrhoea
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isUnusualSleepy
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.isVomiting
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.muacCode
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class AssessmentICCMSummaryFragment : BaseFragment(), View.OnClickListener {
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
        initViews()
        setListeners()
        attachObservers()
    }

    private fun initViews() {
        getClinicTakenData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = IsClinicTaken
            view.addViewElements(
                it,
                false,
                viewModel.otherAssessmentDetails,
                IsClinicTaken,
                FormLayout(viewType = "", id = "", title = "", visibility = "", optionsList = null),
                singleSelectionCallback
            )
            binding.clinicTakenGroup.addView(view)
        }
    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: String, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.otherAssessmentDetails[IsClinicTaken] = selectedID as String
        }

    private fun getClinicTakenData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(getOptionMap(getString(R.string.yes)))
        flowList.add(getOptionMap(getString(R.string.no)))
        return flowList
    }

    private fun setListeners() {
        binding.btnDone.safeClickListener(this)
        binding.etNotes.addTextChangedListener { input ->
            input?.let {
                val resultValue = input.trim().toString()
                if (resultValue.isNotBlank()) {
                    viewModel.otherAssessmentDetails[AssessmentNotes] = resultValue
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
        return viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }?.map { formLayout ->
            AssessmentSummaryModel(
                title = formLayout.titleSummary ?: formLayout.title,
                id = formLayout.id,
                cultureValue = formLayout.titleCulture,
                value = getValueOfKeyFromMap(StringConverter.stringToMap(data), formLayout.id, ICCM),
                noOfDays = formLayout.noOfDays
            )
        }?.toMutableList()
    }

    private fun composeIccmSummaryView(listSummaryData: MutableList<AssessmentSummaryModel>) {
        bindICCMSummaryView(
            getString(R.string.patient_status),
            getString(R.string.seperator_hyphen)
        )
        composeGeneralDangerSignsResult(listSummaryData)
        listSummaryData.filter { it.title?.lowercase() != General_Danger_Signs.lowercase() }.forEach { item ->
            when (item.id) {
                muacCode -> {
                    bindICCMSummaryView(
                        item.title,
                        requireContext().getString(
                            R.string.nutrition_summary,
                            item.value,
                            getNutritionStatus(item.value, requireContext())
                        )
                    )
                }

                isDiarrhoea -> {
                    if (item.value == Yes) {
                        binding.diarrhoeaGroup.visibility = View.VISIBLE
                        bindICCMSummaryView(
                            item.title,
                            requireContext().getString(
                                R.string.nutrition_summary,
                                item.value,
                                getString(R.string.severe_dehydration)
                            )
                        )
                    } else {
                        bindICCMSummaryView(item.title, item.value)
                    }
                }

                hasCough -> {
                    if (item.value == Yes) {
                        binding.coughMalariaGroup.visibility = View.VISIBLE
                        bindICCMSummaryView(
                            item.title,
                            requireContext().getString(
                                R.string.nutrition_summary,
                                item.value,
                                getString(R.string.pneumonia)
                            )
                        )
                    } else {
                        bindICCMSummaryView(item.title, item.value)
                    }
                }

                BreathPerMinute -> {
                    item.value?.let {result ->
                        bindICCMSummaryView(
                            item.title,
                            requireContext().getString(
                                R.string.firstname_lastname,
                                result,
                                getString(R.string.bpm)
                            )
                        )
                    }
                }

                hasFever -> {
                    if (item.value == Yes) {
                        binding.coughMalariaGroup.visibility = View.VISIBLE
                        bindICCMSummaryView(
                            item.title,
                            requireContext().getString(
                                R.string.nutrition_summary,
                                item.value,
                                getString(R.string.malaria)
                            )
                        )
                    } else {
                        bindICCMSummaryView(item.title, item.value)
                    }
                }

                NoOfDaysOfCough, NoOfDaysDiarrhoea, NoOfDaysOfFever -> {
                    item.noOfDays?.let { maxDays ->
                        item.value?.let { enteredDays ->
                            bindICCMSummaryView(
                                item.title,
                                requireContext().getString(
                                    R.string.days_summary,
                                    enteredDays.toDouble().toInt(),
                                    maxDays
                                )
                            )
                        }
                    } ?: kotlin.run {
                        bindICCMSummaryView(item.title, item.value)
                    }
                }

                Amoxicillin.lowercase(), ACT.lowercase(), OrsDispensedStatus, ZincDispensedStatus -> {
                    if (item.value == Dispensed){
                        bindICCMSummaryView(Dispensed, item.title)
                    }
                }

                else -> {
                    bindICCMSummaryView(item.title, item.value)
                }
            }
        }
    }


    private fun composeGeneralDangerSignsResult(listSummaryData: MutableList<AssessmentSummaryModel>) {
        val targetIds = setOf(
            isUnusualSleepy,
            isConvulsionPastFewDays,
            isVomiting,
            isBreastfeed
        )
        var result = DefinedParams.No
        for (assessment in listSummaryData) {
            if (assessment.id in targetIds && assessment.value == Yes) {
                result = Yes
            }
        }
        bindICCMSummaryView(listSummaryData[0].title ?: getString(R.string.general_danger_signs), result)
    }

    private fun bindICCMSummaryView(title: String?, value: String?, valueTextColor: Int? = null) {
        value?.let {result ->
            binding.parentLayout.addView(
                addViewSummaryLayout(
                    title,
                    result,
                    valueTextColor,
                    requireContext()
                )
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
                viewModel.otherAssessmentDetails[NextFollowupDate] =
                    binding.etNextFollowUpDate.text.toString()
                datePickerDialog = null
            }
        }
    }
}