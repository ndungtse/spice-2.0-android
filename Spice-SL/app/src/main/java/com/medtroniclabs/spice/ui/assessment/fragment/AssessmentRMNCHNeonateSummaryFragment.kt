package com.medtroniclabs.spice.ui.assessment.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.StringConverter.stringToMap
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentAssessmentRmnchNeonateSummaryBinding
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.getValueFromMap
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentRMNCHNeonateViewModel
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class AssessmentRMNCHNeonateSummaryFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentAssessmentRmnchNeonateSummaryBinding

    private val assessmentRMNCHNeonateViewModel: AssessmentRMNCHNeonateViewModel by activityViewModels()

    private val viewModel: AssessmentViewModel by activityViewModels()

    private var datePickerDialog: DatePickerDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentRmnchNeonateSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListener()
    }

    private fun initView() {
        assessmentRMNCHNeonateViewModel.assessmentSaveLiveData.value?.data?.let {
            val map = stringToMap(it.assessmentDetails)
            if (map.containsKey(RMNCH.PNC)) {
                addDefaultSummaryView(map)
                showSummaryDetail(
                    map,
                    RMNCH.PNC,
                    binding.motherParentLayout,
                    viewModel.formLayoutsLiveData.value
                )
            }
            if (map.containsKey(RMNCH.PNCNeonatal)) {
                showSummaryDetail(
                    map,
                    RMNCH.PNCNeonatal,
                    binding.neonateParentLayout,
                    assessmentRMNCHNeonateViewModel.formLayoutsLiveData.value
                )
            }
        }
    }

    private fun setListener() {
        binding.btnDone.safeClickListener(this)
        binding.etNextFollowUpDate.safeClickListener(this)

        binding.etNextFollowUpDate.addTextChangedListener {
            binding.btnDone.isEnabled = !it.isNullOrEmpty()
        }
    }

    private fun showSummaryDetail(
        map: Map<*, *>,
        pnc: String,
        parentLayout: LinearLayout,
        value: Resource<FormResponse>?
    ) {
        value?.data?.formLayout?.filter { it.family == pnc && it.isSummary == true }
            ?.forEach { formlayout ->
                formlayout.apply {
                    parentLayout.addView(
                        AssessmentCommonUtils.addViewSummaryLayout(
                            titleSummary ?: (titleCulture ?: title),
                            getValueFromMap(
                                map as HashMap<String, Any>,
                                id,
                                viewType,
                                pnc,
                                isBooleanAnswer,
                                Triple(
                                    getString(R.string.yes),
                                    getString(R.string.no),
                                    getString(R.string.hyphen_symbol)
                                )
                            ),
                            null,
                            requireContext()
                        )
                    )
                }
            }
    }


    private fun addDefaultSummaryView(map: HashMap<String, Any>) {

        val title: String = when (viewModel.workflowName) {
            RMNCH.ANC -> getString(R.string.anc_visit)
            RMNCH.ChildHoodVisit -> getString(R.string.child_hood_visit)
            RMNCH.PNC -> getString(R.string.pnc_visit)
            else -> getString(R.string.hyphen_symbol)
        }

        binding.motherParentLayout.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title,
                getValueFromMap(
                    map,
                    RMNCH.visitNo,
                    ViewType.VIEW_TYPE_FORM_EDITTEXT,
                    viewModel.workflowName,
                    false,
                    Triple(
                        getString(R.string.yes),
                        getString(R.string.no),
                        getString(R.string.hyphen_symbol)
                    )
                ),
                null,
                requireContext()
            )
        )

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnDone -> {
                if (binding.etNextFollowUpDate.text.isNotEmpty()) {
                    viewModel.addOtherDetailsToType(AssessmentDefinedParams.RMNCH.lowercase())
                    assessmentRMNCHNeonateViewModel.updateOtherAssessmentDetails(
                        viewModel.otherAssessmentDetails,
                        viewModel.getCurrentLocation(),
                        viewModel.assessmentUpdateLiveData
                    )
                }
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