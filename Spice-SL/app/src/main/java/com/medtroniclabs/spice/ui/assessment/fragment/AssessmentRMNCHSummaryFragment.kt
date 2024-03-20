package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentRmnchSummaryBinding
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class AssessmentRMNCHSummaryFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentRmnchSummaryBinding

    private val viewModel: AssessmentViewModel by activityViewModels()

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
        binding.btnDone.setOnClickListener(this)
    }

    private fun initSummaryViewByWorkFlowName() {
        viewModel.assessmentSaveLiveData.value?.data?.let { mapString ->
            val map = StringConverter.stringToMap(mapString.assessmentDetails)
            binding.parentLayout.removeAllViews()
            viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }
                ?.forEach { data ->
                    with(data) {
                        binding.parentLayout.addView(
                            AssessmentCommonUtils.addViewSummaryLayout(
                                titleSummary ?: (titleCulture ?: title),
                                getValueFromMap(map, id, family, viewType),
                                null,
                                requireContext()
                            )
                        )
                    }
                }
        }
    }

    private fun getValueFromMap(
        map: HashMap<String, Any>,
        id: String,
        groupName: String?,
        viewType: String
    ): String {
        if (map.containsKey(groupName)) {
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
                    if (value is String) {
                        return value
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
                requireActivity().finish()
            }
        }
    }


}