package com.medtroniclabs.spice.ui.followup.fragment

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setDialogWidthAndHeightAsWrapPercent
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.FU_TYPE_HH_VISIT
import com.medtroniclabs.spice.data.FollowUpPatientModel
import com.medtroniclabs.spice.databinding.FragmentFollowUpDialogBinding
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentActivity
import com.medtroniclabs.spice.ui.followup.viewmodel.FollowUpViewModel
import com.medtroniclabs.spice.ui.home.ToolsActivity

class FollowUpDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentFollowUpDialogBinding
    private val viewModel: FollowUpViewModel by activityViewModels()
    private var followUpDetail: FollowUpPatientModel? = null

    companion object {
        const val TAG = "FollowUpDialogFragment"
        fun newInstance(followUpDetail: FollowUpPatientModel): FollowUpDialogFragment {
            val fragment = FollowUpDialogFragment()
            fragment.followUpDetail = followUpDetail
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowUpDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        applyOrientationChange()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.btnCall.setOnClickListener {
            CallResultDialogFragment.newInstance()
                .show(childFragmentManager, CallResultDialogFragment.TAG)
        }
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btnAssessment.setOnClickListener {
            followUpDetail?.let {
                dismiss()
                val intent = Intent(requireContext(), AssessmentActivity::class.java)
                intent.putExtra(DefinedParams.MemberID, it.localPatientId)
                intent.putExtra(DefinedParams.MenuId, it.encounterType?.lowercase())
                startActivity(intent)
            }
        }

        followUpDetail?.let { details ->
            with(binding) {
                tvTitle.text = getPatientName(details.name, details.dateOfBirth, details.gender)
                tvReasonText.text = details.reason ?: getString(R.string.hyphen_symbol)
                tvPatientStatusText.text = details.patientStatus ?: getString(R.string.hyphen_symbol)
                tvVillageText.text = details.village ?: getString(R.string.hyphen_symbol)
                tvLandmarkText.text = details.landmark ?: getString(R.string.hyphen_symbol)
                tvHHNameText.text = details.householdName ?: getString(R.string.hyphen_symbol)
                tvMemberIDText.text = details.patientId ?: getString(R.string.hyphen_symbol)
                tvCallsMadeText.text = details.remainingRetryAttempt.toString()

                if (details.type == FU_TYPE_HH_VISIT)
                    btnAssessment.visible()
                else
                    btnAssessment.gone()
            }
        }
    }

    private fun getPatientName(
        name: String?,
        dob: String?,
        gender: String?
    ): String {
        return getString(
            R.string.household_summary_member_info,
            name,
            CommonUtils.getAgeFromDob(
                dob,
                getString(R.string.months)
            ),
            CommonUtils.getGenderText(gender, requireContext())
        )
    }

    private fun applyOrientationChange() {
        val tabletSize =
            resources.getBoolean(R.bool.isLargeTablet) || resources.getBoolean(R.bool.isTablet)
        if (tabletSize) {
            forPhoneAndTab(65, 75)
        } else {
            forPhoneAndTab(65, 90)
        }

    }

    private fun forPhoneAndTab(landscape: Int, portrait: Int) {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                setDialogWidthAndHeightAsWrapPercent(landscape)
            }

            Configuration.ORIENTATION_PORTRAIT -> {
                setDialogWidthAndHeightAsWrapPercent(portrait)
            }
        }
    }

    private fun startAssessmentActivity(menuId: String, memberId: Long) {

    }
}