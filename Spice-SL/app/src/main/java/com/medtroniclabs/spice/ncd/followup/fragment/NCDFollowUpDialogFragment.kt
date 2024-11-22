package com.medtroniclabs.spice.ncd.followup.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setDialogWidthAndHeightAsWrapPercent
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.databinding.FragmentNcdFollowUpDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.getDaysString
import com.medtroniclabs.spice.ncd.followup.viewmodel.NCDFollowUpViewModel
import com.medtroniclabs.spice.ui.mypatients.PatientSelectionListenerForFollowUp

class NCDFollowUpDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentNcdFollowUpDialogBinding
    private val viewModel: NCDFollowUpViewModel by activityViewModels()
    private var listener: PatientSelectionListenerForFollowUp? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNcdFollowUpDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "NCDFollowUpDialogFragment"
        fun newInstance(listener: PatientSelectionListenerForFollowUp): NCDFollowUpDialogFragment {
            val frag = NCDFollowUpDialogFragment()
            frag.listener = listener
            return frag
        }
    }

    override fun onStart() {
        super.onStart()
        applyOrientationChange()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        // Setup button click listeners
        binding.apply {
            btnCall.setOnClickListener {
                dismiss()
                viewModel.selectedPatient?.let { listener?.onSelectedPatientForCall(it) }
            }

            ivClose.setOnClickListener {
                viewModel.selectedPatient = null
                dismiss()
            }

            btnAssessment.setOnClickListener {
                dismiss()
                viewModel.selectedPatient?.let { listener?.onSelectedPatientForAssessment(it) }
            }
        }

        // Populate UI elements with patient data
        viewModel.selectedPatient?.let { patient ->
            with(binding) {
                val hyphen = getString(R.string.hyphen_symbol)
                val name = patient.name ?: hyphen
                val gender = patient.gender?.lowercase()?.capitalizeFirstChar() ?: hyphen
                val age = patient.age?.toString() ?: ""

                // Patient info
                tvTitle.text = getString(R.string.household_summary_member_info, name, age, gender)

                // Diagnosis and referred reasons
                tvReasonLabel.text = getString(R.string.diagnosis)
                tvReasonText.text = patient.referredReasons
                    ?.filterNot { it.isBlank() }
                    ?.joinToString(", ")
                    ?: hyphen

                setField(tvStartDateLabel,
                    tvStartDateText,
                    getString(R.string.day_since_last_review),
                    patient.referredDateSince?.let { getString(getDaysString(it), it) })

                setField(
                    tvCountyLabel,
                    tvCountyText,
                    getString(R.string.county),
                    patient.countyName
                )
                setField(
                    tvSubCountyLabel,
                    tvSubCountyText,
                    getString(R.string.sub_county),
                    patient.subCountyName
                )
                setField(
                    tvChuLabel, tvChuText,
                    getString(R.string.community_health_unit),
                    patient.communityHealthUnitName
                )
                setField(
                    tvVillageLabel,
                    tvVillageText,
                    getString(R.string.village),
                    patient.villageName
                )
                setField(
                    tvLandmarkLabel,
                    tvLandmarkText,
                    getString(R.string.landmark),
                    patient.landmark
                )
                setField(
                    tvNationalIDLabel, tvNationalIDText,
                    getString(R.string.national_id),
                    patient.identityValue
                )
                // Show buttons
                btnAssessment.visible()
                btnCall.visible()
            }
        }
    }

    // General fields with labels and data
    private fun setField(labelView: TextView, dataView: TextView, label: String, data: String?) {
        labelView.text = label
        dataView.text = data?.capitalizeFirstChar() ?: getString(R.string.hyphen_symbol)
    }
}