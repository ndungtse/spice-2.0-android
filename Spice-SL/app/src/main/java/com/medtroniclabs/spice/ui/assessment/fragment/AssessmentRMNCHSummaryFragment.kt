package com.medtroniclabs.spice.ui.assessment.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentRmnchSummaryBinding
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class AssessmentRMNCHSummaryFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentRmnchSummaryBinding

    private val viewModel: AssessmentViewModel by activityViewModels()

    private var datePickerDialog: DatePickerDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRmnchSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSummaryViewByWorkFlowName()
        setListener()
    }

    private fun setListener() {
        binding.btnDone.safeClickListener(this)
        binding.etNextFollowUpDate.safeClickListener(this)
    }

    private fun initSummaryViewByWorkFlowName() {
        viewModel.assessmentStringLiveData.value?.let { mapString ->
            val map = StringConverter.stringToMap(mapString)
            binding.parentLayout.removeAllViews()
            viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }
                ?.forEach { data ->
                    with(data) {
                        binding.parentLayout.addView(
                            AssessmentCommonUtils.addViewSummaryLayout(
                                titleSummary ?: (titleCulture ?: title),
                                getValueFromMap(
                                    map,
                                    id,
                                    family,
                                    viewType,
                                    AssessmentDefinedParams.RMNCH.lowercase(),
                                    isBooleanAnswer
                                ),
                                null,
                                requireContext()
                            )
                        )
                    }
                }
        }
    }

    private fun getValueFromMap(
        resultMap: HashMap<String, Any>,
        id: String,
        groupName: String?,
        viewType: String,
        workflowName: String?,
        isBooleanAnswer: Boolean
    ): String {
        if (resultMap.containsKey(workflowName)) {
            val map = resultMap[workflowName]
            if (map is Map<*, *> && map.containsKey(groupName)) {
                val actualMap = map[groupName]
                if (actualMap is Map<*, *>) {
                    val value = actualMap[id]
                    if (viewType == ViewType.VIEW_TYPE_FORM_DATEPICKER && value is String) {
                        return DateUtils.convertDateFormat(
                            value,
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                            DateUtils.DATE_ddMMyyyy
                        )
                    } else {
                        when (value) {
                            is String -> {
                                return value
                            }

                            is Boolean -> {
                                return if (isBooleanAnswer) {
                                    if (value) getString(R.string.yes) else getString(R.string.no)
                                } else {
                                    value.toString()
                                }
                            }
                        }
                    }
                }
            }
        }
        return getString(R.string.hyphen_symbol)
    }

    companion object {
        const val TAG: String = "AssessmentRMNCHSummaryFragment"
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnDone -> {
                viewModel.addOtherDetailsToType(AssessmentDefinedParams.RMNCH.lowercase())
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
                viewModel.otherAssessmentDetails[AssessmentDefinedParams.NextFollowupDate] =
                    binding.etNextFollowUpDate.text.toString()
                datePickerDialog = null
            }
        }
    }


}