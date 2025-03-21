package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentTBTreatmentBinding
import com.medtroniclabs.spice.db.entity.RxBuddyDetails
import com.medtroniclabs.spice.db.entity.TreatmentDetailsEntity
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TBRxBuddyFragment : BaseFragment() {
    private lateinit var binding: FragmentTBTreatmentBinding
    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTBTreatmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.treatmentCardTitle.text = getString(R.string.rx_buddy_details)
        createSummaryView(createTreatmentSummaryData())
    }

    private fun createTreatmentSummaryData(): List<AssessmentSummaryModel>? {
        return mutableListOf(
            AssessmentSummaryModel(
                title = getString(R.string.rx_buddy_name),
                value = checkNullValue(arguments?.getString(DefinedParams.RxBuddyName))
            ),
            AssessmentSummaryModel(
                title = getString(R.string.relation_ship_with_patient),
                value = checkNullValue(arguments?.getString(DefinedParams.RxRelationShip))
            ),
            AssessmentSummaryModel(
                title = getString(R.string.contact_no),
                value = checkNullValue(arguments?.getString(DefinedParams.RxPhoneNo))
            ),
            AssessmentSummaryModel(
                title = getString(R.string.rx_buddy_monitoring_sheet_provided),
                value = capitalizeYesNo(arguments?.getBoolean(DefinedParams.RxMonitoringSheetProvided).toString())
            )
        )
    }

    private fun createSummaryView(
        listSummaryData: List<AssessmentSummaryModel>?
    ) {
        listSummaryData?.let {summaryData ->
            binding.emptyErrorMessage.visibility = View.GONE
            binding.llParentLayout.visibility = View.VISIBLE
            binding.llParentLayout.removeAllViews()
            composeTbSummaryView(summaryData)
        } ?: kotlin.run {
            showErrorInSummary()
        }
    }

    private fun composeTbSummaryView(listSummaryData: List<AssessmentSummaryModel>) {
        listSummaryData.forEach { item ->
            bindTbSummaryView(item.title,item.value)
        }
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.llParentLayout.visibility = View.GONE
    }

    private fun bindTbSummaryView(title: String?, value: String?, valueTextColor: Int? = null) {
        binding.llParentLayout.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title,
                value,
                valueTextColor,
                requireContext()
            )
        )
    }

    private fun checkNullValue(input: String?): String {
        return input ?: "-"
    }

    private fun capitalizeYesNo(value: String): String {
        return value.lowercase().replaceFirstChar { it.uppercase() }
    }

    companion object{
        const val TAG = "TBRxBuddyFragment"
    }
}