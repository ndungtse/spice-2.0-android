package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentRmnchSummaryBinding
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class AssessmentRMNCHSummaryFragment : BaseFragment() {

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
    }

    private fun initSummaryViewByWorkFlowName() {
        viewModel.assessmentSaveLiveData.value?.data?.let { mapString ->
            val map = StringConverter.stringToMap(mapString)
            binding.parentLayout.removeAllViews()
            viewModel.formLayoutsLiveData.value?.data?.formLayout?.filter { it.isSummary == true }
                ?.forEach { data ->
                    with(data) {
                        binding.parentLayout.addView(
                            AssessmentCommonUtils.addViewSummaryLayout(
                                titleCulture ?: title,
                                getValueFromMap(map, id, family),
                                null,
                                requireContext()
                            )
                        )
                    }
                }
        }
    }

    private fun getValueFromMap(map: HashMap<String, Any>, id: String, groupName: String?): String {
        if (map.containsKey(groupName)) {
            val actualMap = map[groupName]
            if (actualMap is Map<*, *>) {
                val value = actualMap[id]
                if (value is String) {
                    return value
                }
            }
        }
        return getString(R.string.hyphen_symbol)
    }

    companion object {
        const val TAG: String = "AssessmentRMNCHSummaryFragment"
    }

}