package com.medtroniclabs.spice.ui.assessment.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.calculateEstimatedDeliveryDate
import com.medtroniclabs.spice.common.DateUtils.calculateGestationalAge
import com.medtroniclabs.spice.databinding.FragmentBioDataBinding
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.MemberClinicalEntity
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BioDataFragment : BaseFragment() {

    private lateinit var binding: FragmentBioDataBinding

    private val viewModel: AssessmentViewModel by activityViewModels()

    companion object {
        const val TAG = "BioDataFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBioDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
        val title: String
        when (viewModel.workflowName) {
            RMNCH.ANC -> {
                title = getString(R.string.anc)
                showAncRelatedInformation(entity)
            }

            RMNCH.PNC -> {
                title = getString(R.string.pnc)
            }

            RMNCH.ChildHoodVisit -> {
                title = getString(R.string.child_hood_visit)
            }

            else -> {
                title = getString(R.string.hyphen_symbol)
            }
        }

        binding.llPatientInfo.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title = title,
                value = getVisitCount(entity?.visitCount),
                null,
                binding.root.context
            )
        )
    }

    private fun showAncRelatedInformation(entity: MemberClinicalEntity?) {
        entity?.apply {
            binding.llPatientInfo.addView(
                AssessmentCommonUtils.addViewSummaryLayout(
                    title = getString(R.string.last_menstrual_period),
                    value = DateUtils.convertDateFormat(
                        clinicalDate,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DATE_ddMMyyyy
                    ),
                    context = binding.llPatientInfo.context
                )
            )
            val lastMenstrualDate = getLastMenstrualDate(clinicalDate)
            createSummary(
                getString(R.string.gestational_age),
                "${calculateGestationalAge(lastMenstrualDate).first} ${getString(R.string.weeks)}"
            )
            val estimatedDeliveryDate = calculateEstimatedDeliveryDate(lastMenstrualDate)
            val formattedEstimatedDeliveryDate = getDateFormat().format(estimatedDeliveryDate.time)
            createSummary(
                getString(R.string.estimated_delivery_date),
                formattedEstimatedDeliveryDate
            )
        }
    }

    private fun getDateFormat(): SimpleDateFormat {
        return SimpleDateFormat(
            DATE_ddMMyyyy,
            Locale.getDefault()
        )
    }

    private fun getLastMenstrualDate(clinicalDate: String): Calendar {
        // Define the format of the input date string
        val lastMenstrualDateString = DateUtils.convertDateFormat(
            clinicalDate,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DATE_ddMMyyyy
        )
        return Calendar.getInstance().apply {
            time = getDateFormat().parse(lastMenstrualDateString)
        }
    }

    private fun createSummary(title: String, value: String) {
        binding.llPatientInfo.addView(
            AssessmentCommonUtils.addViewSummaryLayout(
                title = title,
                value = value,
                context = binding.llPatientInfo.context
            )
        )
    }

    private fun getVisitCount(visitCount: Long?): String {
        return if (visitCount == null) {
            "1"
        } else {
            (visitCount + 1).toString()
        }
    }

    private fun initView() {
        viewModel.getMemberDetailsById()
    }

    private fun showPatientBioData(data: AssessmentMemberDetails?) {
        data?.apply {
            binding.patientName.tvKey.text = getString(R.string.name)
            binding.patientName.tvValue.text = name.capitalizeFirstChar()
            binding.patientId.tvKey.text = getString(R.string.patient_id)
            binding.patientId.tvValue.text = patientId
            binding.gender.tvKey.text = getString(R.string.gender)
            binding.gender.tvValue.text = gender.capitalizeFirstChar()
            binding.dobAge.tvKey.text = getString(R.string.age)
            binding.dobAge.tvValue.text = getAgeValue(
                CommonUtils.getAgeFromDob(
                    dateOfBirth,
                    requireContext().getString(R.string.months)
                )
            )
            viewModel.workflowName?.let { workFlowName ->
                patientId?.let { patientId ->
                    viewModel.getPatientVisitCountByType(workFlowName, patientId)
                }
            }
        }
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
