package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.FragmentBioDataBinding
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentRMNCHNeonateViewModel
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class BioDataChildFragment : BaseFragment() {

    private lateinit var binding: FragmentBioDataBinding
    private val assessmentRMNCHNeonateViewModel: AssessmentRMNCHNeonateViewModel by activityViewModels()
    private val viewModel: AssessmentViewModel by activityViewModels()

    companion object {
        const val TAG = "BioDataFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBioDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachObserver()
        viewModel.memberDetailsLiveData.value?.data?.patientId?.let {
            assessmentRMNCHNeonateViewModel.getMemberDetailsByParentId(it)
        }
    }

    private fun attachObserver() {
        assessmentRMNCHNeonateViewModel.childMemberDetailsLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()

                }

                ResourceState.ERROR -> {
                    hideProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resource.data?.let { data ->
                       if (data.isNotEmpty())
                            showPatientBioData(data[0])
                    }
                }
            }
        }
    }

    private fun showPatientBioData(data: HouseholdMemberEntity?) {
        data?.apply {
            binding.patientName.tvKey.text = getString(R.string.name)
            binding.patientName.tvValue.text = name.capitalizeFirstChar()
            binding.patientId.tvKey.text = getString(R.string.patient_id)
            binding.patientId.tvValue.text = patientId
            binding.gender.tvKey.text = getString(R.string.gender)
            binding.gender.tvValue.text = gender.capitalizeFirstChar()
            binding.dobAge.tvKey.text = getString(R.string.age)
            binding.dobAge.tvValue.text = getAgeValue(
                CommonUtils.getAgeFromDOB(
                    dateOfBirth,
                    requireContext()
                )
            )
            binding.llPatientInfo.addView(
                AssessmentCommonUtils.addViewSummaryLayout(
                    title = getString(R.string.pnc),
                    value = getVisitCount(),
                    null,
                    binding.root.context
                )
            )
        }
    }

    private fun getVisitCount(): String {
        viewModel.memberClinicalLiveData.value?.visitCount?.let {
            return (it + 1L).toString()
        }
        return getString(R.string.hyphen_symbol)
    }

    private fun getAgeValue(ageFromDob: String): String {
        return if (!ageFromDob.contains(" "))
            requireContext().getString(
                R.string.firstname_lastname,
                ageFromDob,
                getString(R.string.years)
            )
        else
            ageFromDob
    }

}