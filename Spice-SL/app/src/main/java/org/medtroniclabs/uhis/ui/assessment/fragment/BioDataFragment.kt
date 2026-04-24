package org.medtroniclabs.uhis.ui.assessment.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DateUtils.DATE_ddMMyyyy
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.databinding.FragmentBioDataBinding
import org.medtroniclabs.uhis.db.entity.MemberClinicalEntity
import org.medtroniclabs.uhis.db.entity.PregnancyDetail
import org.medtroniclabs.uhis.formgeneration.extension.capitalizeFirstChar
import org.medtroniclabs.uhis.mappingkey.PregnantWomen.PREGNANCY_MAX_AGE_THRESHOLD
import org.medtroniclabs.uhis.mappingkey.PregnantWomen.PREGNANCY_MIN_AGE_THRESHOLD
import org.medtroniclabs.uhis.model.assessment.AssessmentMemberDetails
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.MenuConstants
import org.medtroniclabs.uhis.ui.assessment.AssessmentCommonUtils
import org.medtroniclabs.uhis.ui.assessment.rmnch.RMNCH
import org.medtroniclabs.uhis.ui.assessment.viewmodel.AssessmentViewModel

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
        stabilizePregnancyOutcomeLayout()
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

        viewModel.pregnancyDetailLiveData.observe(viewLifecycleOwner) { pregnancyDetail ->
            handlePregnancyDetail(pregnancyDetail)
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
            val age = CommonUtils.getAgeFromDOB(
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
                if (gender.equals(DefinedParams.GENDER_FEMALE, true) &&
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

            if (viewModel.menuId == MenuConstants.PREGNANCY_OUTCOME) {
                binding.gender.root.gone()
                binding.mobileNumber.root.gone()
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
        } else {
            ageFromDob.ifEmpty {
                requireContext().getString(R.string.seperator_hyphen)
            }
        }

    /**
     * Handles details w.r.t to pregnancy details
     */
    private fun handlePregnancyDetail(pregnancyDetail: PregnancyDetail?) {
        if (viewModel.menuId.equals(MenuConstants.PREGNANCY_OUTCOME, ignoreCase = true)) {
            // Shows "Total ANC services received" field only for pregnancy outcome workflow
            val ancVisitNo = pregnancyDetail?.ancVisitNo
            binding.totalAncServices.root.visible()
            binding.totalAncServices.tvKey.text = getString(R.string.total_anc_services_received)
            binding.totalAncServices.tvValue.text = ancVisitNo?.toString() ?: getString(R.string.separator_double_hyphen)
        } else if (RMNCH.ANC.equals(viewModel.workflowName, ignoreCase = true)) {
            //  Highlight if  1. Age <18 years 2. Age >35 years for ANC workflow
            val dateOfBirth = viewModel.memberDetailsLiveData.value
                ?.data
                ?.dateOfBirth
            val lmp = pregnancyDetail?.lastMenstrualPeriod
            if (dateOfBirth != null && lmp != null) {
                val calculatedAge = DateUtils.calculateAgeToDate(dateOfBirth, lmp)
                if (calculatedAge < PREGNANCY_MIN_AGE_THRESHOLD) {
                    binding.dobAge.tvValue.setTextColor(Color.RED)
                } else if (calculatedAge > PREGNANCY_MAX_AGE_THRESHOLD) {
                    binding.dobAge.tvValue.setTextColor(Color.RED)
                }
            }
        }
    }

    private fun stabilizePregnancyOutcomeLayout() {
        if (!viewModel.menuId.equals(MenuConstants.PREGNANCY_OUTCOME, ignoreCase = true)) {
            return
        }
        val patientInfoParent = binding.llPatientInfo.parent as? ViewGroup
        patientInfoParent?.layoutTransition = null
        binding.llPatientInfo.layoutTransition = null

        // Pre-apply final visibility state to avoid animated height jumps across initial async updates.
        binding.gender.root.gone()
        binding.mobileNumber.root.gone()
        binding.totalAncServices.root.visible()
        binding.totalAncServices.tvValue.text = getString(R.string.separator_double_hyphen)
    }
}
