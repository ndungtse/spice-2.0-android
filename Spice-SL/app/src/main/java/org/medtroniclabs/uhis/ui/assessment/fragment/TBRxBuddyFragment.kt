package org.medtroniclabs.uhis.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.databinding.FragmentTBTreatmentBinding
import org.medtroniclabs.uhis.db.entity.RxBuddyDetails
import org.medtroniclabs.uhis.model.AssessmentSummaryModel
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.assessment.AssessmentCommonUtils
import org.medtroniclabs.uhis.ui.assessment.viewmodel.AssessmentViewModel

@AndroidEntryPoint
class TBRxBuddyFragment : BaseFragment() {
    private lateinit var binding: FragmentTBTreatmentBinding
    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTBTreatmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.treatmentCardTitle.text = getString(R.string.rx_buddy_details)
        viewModel.rxBuddyDetailsLiveData.observe(viewLifecycleOwner) {
            createSummaryView(createTreatmentSummaryData(it))
        }
    }

    private fun createTreatmentSummaryData(rxBuddyDetails: RxBuddyDetails): List<AssessmentSummaryModel>? {
        val list = mutableListOf<AssessmentSummaryModel>()
        list.add(
            AssessmentSummaryModel(
                title = getString(R.string.rx_buddy_name),
                value = checkNullValue(rxBuddyDetails.name),
            ),
        )

        list.add(
            AssessmentSummaryModel(
                title = getString(R.string.relation_ship_with_patient),
                value = checkNullValue(rxBuddyDetails.relationship),
            ),
        )

        rxBuddyDetails.otherRelationship?.let {
            list.add(
                AssessmentSummaryModel(
                    title = getString(R.string.other_relationship),
                    value = checkNullValue(it),
                ),
            )
        }

        list.add(
            AssessmentSummaryModel(
                title = getString(R.string.contact_no),
                value = checkNullValue("+${SecuredPreference.getPhoneNumberCode()} ${rxBuddyDetails.phoneNumber}"),
            ),
        )

        list.add(
            AssessmentSummaryModel(
                title = getString(R.string.rx_buddy_monitoring_sheet_provided),
                value = capitalizeYesNo(rxBuddyDetails.isMonitorSheetProvider),
            ),
        )

        return list
    }

    private fun createSummaryView(listSummaryData: List<AssessmentSummaryModel>?) {
        listSummaryData?.let { summaryData ->
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
            bindTbSummaryView(item.title, item.value)
        }
    }

    private fun showErrorInSummary() {
        binding.emptyErrorMessage.visibility = View.VISIBLE
        binding.llParentLayout.visibility = View.GONE
    }

    private fun bindTbSummaryView(
        title: String?,
        value: String?,
        valueTextColor: Int? = null,
    ) {
        binding.llParentLayout.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title,
                value,
                valueTextColor,
                requireContext(),
            ),
        )
    }

    private fun checkNullValue(input: String?): String = input ?: "-"

    private fun capitalizeYesNo(value: Boolean): String = if (value) getString(R.string.yes) else getString(R.string.no)

    companion object {
        const val TAG = "TBRxBuddyFragment"
    }
}
