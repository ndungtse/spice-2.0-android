package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentBioDataBinding
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class BioDataFragment : BaseFragment() {
    private lateinit var binding: FragmentBioDataBinding

    private val viewModel: AssessmentViewModel by activityViewModels()

    private var isCbs: Boolean = false

    companion object {
        const val TAG = "BioDataFragment"

        fun newInstance(isCbs: Boolean): BioDataFragment {
            val fragment = BioDataFragment()
            fragment.isCbs = isCbs
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBioDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.memberDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    viewModel.getPregnancyDetailInformation()
                    showPatientBioData(resourceState.data)
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }

        viewModel.memberClinicalLiveData.observe(viewLifecycleOwner) { entity ->
            showPatientOtherInformation(entity)
        }
    }

    private fun showPatientOtherInformation(entity: MemberClinicalEntity?) {
        val visitCount = getVisitCount(entity?.visitCount)
        val title = when (viewModel.workflowName) {
            RMNCH.ChildHoodVisit -> {
                getString(R.string.child_hood_visit)
            }

            else -> {
                null
            }
        }

        if (title != null) {
            binding.llPatientInfo.addView(
                AssessmentCommonUtils.addViewSummaryLayout(
                    title = title,
                    value = visitCount.toString(),
                    null,
                    binding.root.context,
                ),
            )
        }
    }

    private fun getVisitCount(visitCount: Long?): Long =
        if (visitCount == null) {
            1L
        } else {
            (visitCount + 1)
        }

    private fun initView() {
        viewModel.getMemberDetailsById()
    }

    private fun showPatientBioData(data: AssessmentMemberDetails?) {
        data?.apply {
            if (isCbs) {
                binding.householdNo.root.visible()
                binding.householdNo.tvKey.text = getString(R.string.household_no)
                binding.householdNo.tvValue.text = householdNo?.toString() ?: getString(R.string.separator_double_hyphen)
            } else {
                binding.householdNo.root.gone()
            }

            binding.patientName.tvKey.text = getString(R.string.name)
            binding.patientName.tvValue.text = name.capitalizeFirstChar()
            binding.patientId.tvKey.text = getString(R.string.patient_id)
            binding.patientId.tvValue.text = patientId ?: getString(R.string.separator_double_hyphen)
            binding.gender.tvKey.text = getString(R.string.gender)
            binding.gender.tvValue.text = gender.capitalizeFirstChar()
            binding.dobAge.tvKey.text = getString(R.string.age)
            var age = CommonUtils.getAgeFromDOB(
                dateOfBirth,
                requireContext(),
            )
            binding.dobAge.tvValue.text = getAgeValue(
                age,
            )

            // Show mobile number if it exists
            if (!phoneNumber.isNullOrBlank()) {
                binding.mobileNumber.root.visible()
                binding.mobileNumber.tvKey.text = getString(R.string.mobile_number)
                binding.mobileNumber.tvValue.text = phoneNumber
            } else {
                binding.mobileNumber.root.gone()
            }

            if (viewModel.menuId.equals(MenuConstants.CBS_MENU_ID, true)) {
                binding.dateOfOccurrence.root.visible()
                binding.dateOfOccurrence.tvKey.text = getString(R.string.date_of_occurrence)
                binding.dateOfOccurrence.tvValue.text = DateUtils.getTodayDateDDMMYYYY(DATE_ddMMyyyy)
                if (gender.equals(DefinedParams.female, true) &&
                    doesShowPregnant(dateOfBirth)
                ) {
                    binding.pregnancy.root.visible()
                    binding.pregnancy.tvKey.text = getString(R.string.pregnant)
                    binding.pregnancy.tvValue.text = CommonUtils.getBooleanAsString(isPregnant ?: false).capitalizeFirstChar()
                } else {
                    binding.pregnancy.root.gone()
                }
                viewModel.triggerGetForm()
            } else {
                binding.dateOfOccurrence.root.gone()
                binding.pregnancy.root.gone()

                viewModel.ageInMonth.postValue(age)
                viewModel.workflowName?.let { viewModel.getPatientVisitCountByType(it, id) }
            }
        }
    }

    private fun doesShowPregnant(dateOfBirth: String): Boolean {
        val ageAndWeek = DateUtils.getV2YearMonthAndWeek(dateOfBirth)
        val ageYears = ageAndWeek.years
        val ageMonths = ageAndWeek.months
        val ageWeeks = ageAndWeek.weeks
        val ageDays = ageAndWeek.days
        return !(
            (ageYears !in RMNCH.PREGNANCY_MIN_AGE..RMNCH.PREGNANCY_MAX_AGE) ||
                (ageYears == RMNCH.PREGNANCY_MAX_AGE && (ageMonths + ageWeeks + ageDays) != 0)
        )
    }

    private fun getAgeValue(ageFromDob: String): String =
        if (!ageFromDob.contains(" ") && ageFromDob.isNotEmpty()) {
            requireContext().getString(
                R.string.firstname_lastname,
                ageFromDob,
                getString(R.string.years),
            )
        } else if (ageFromDob.isEmpty()) {
            requireContext().getString(R.string.seperator_hyphen)
        } else {
            ageFromDob
        }
}
