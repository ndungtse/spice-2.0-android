package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams.TB
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.FragmentTBTreatmentBinding
import com.medtroniclabs.spice.db.entity.TreatmentDetailsEntity
import com.medtroniclabs.spice.mappingkey.RxBuddy
import com.medtroniclabs.spice.model.AssessmentSummaryModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils.getValueOfKeyFromMap
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TBTreatmentFragment : BaseFragment() {
    private lateinit var binding: FragmentTBTreatmentBinding
    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTBTreatmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        addObserver()
    }

    private fun addObserver() {
        viewModel.treatmentDetailsLiveData.observe(viewLifecycleOwner){ resourceState ->
            when(resourceState.state){
                ResourceState.LOADING -> {
                    showProgress()
                }
                ResourceState.ERROR -> {
                    hideProgress()
                }
                ResourceState.SUCCESS -> {
                    hideProgress()
                    bindData(resourceState.data)
                }
            }
        }
    }

    private fun initView() {
        viewModel.getTreatmentDetails(viewModel.selectedHouseholdMemberId)
    }

    private fun bindData(treatmentDetails: TreatmentDetailsEntity?) {
        treatmentDetails?.let {
            createSummaryView(createTreatmentSummaryData(treatmentDetails))
        }
    }

    private fun createTreatmentSummaryData(td: TreatmentDetailsEntity): List<AssessmentSummaryModel>? {
       return mutableListOf(
           AssessmentSummaryModel(
               title = getString(R.string.diagnoses),
               value = td.diagnoses
           ),
           AssessmentSummaryModel(
               title = getString(R.string.date_diagnosed),
               value = td.diagnosedDate
           ),
           AssessmentSummaryModel(
               title = getString(R.string.treatment_start_date),
               value = td.treatmentStartDate
           ),
           AssessmentSummaryModel(
               title = getString(R.string.health_unit_no),
               value = td.healthUnitNo.toString()
           ),
           AssessmentSummaryModel(
               title = getString(R.string.ic_district_tb_no),
               value = td.icDistrictTBNo.toString()
           ),
           AssessmentSummaryModel(
               title = getString(R.string.type_of_drug),
               value = td.typeOfDrug
           ),
           AssessmentSummaryModel(
               title = getString(R.string.no_of_tablets_given_for_tb),
               value = td.noOfTabletsGivenForTB.toString()
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

    companion object {
        const val TAG = "TBTreatmentFragment"
    }
}