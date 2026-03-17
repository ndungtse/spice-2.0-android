package org.medtroniclabs.uhis.ui.medicalreview.epi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.getDisplayDimensions
import org.medtroniclabs.uhis.databinding.DialogMissedVaccinationBinding
import org.medtroniclabs.uhis.formgeneration.extension.markMandatory
import org.medtroniclabs.uhis.model.medicalreview.VaccinationDetail
import org.medtroniclabs.uhis.ui.BaseDialogFragment
import org.medtroniclabs.uhis.ui.medicalreview.epi.adapter.MissedVaccinationAdapter
import org.medtroniclabs.uhis.ui.medicalreview.epi.viewmodel.ImmunisationViewModel

class MissedImmunisationDialogFragment : BaseDialogFragment() {
    private lateinit var binding: DialogMissedVaccinationBinding
    private val viewModel: ImmunisationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogMissedVaccinationBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_rect_white)
        isCancelable = false
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val (targetWidth, targetHeight) = getDisplayDimensions(requireActivity(), 0.5, 0.8)
        dialog?.window?.setLayout(targetWidth, targetHeight)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        clickListener()
        viewModel.setUserJourney(AnalyticsDefinedParams.MissedVaccination)
    }

    private fun initView() {
        val list = mutableListOf<VaccinationDetail>()
        // Adding dummy object for header
        list.add(
            VaccinationDetail(type = "Week", vaccineName = "", scheduledDate = "", doseClosureWeeks = "", displayOrder = 0, category = "", vaccineOrder = 0),
        )
        list.addAll(viewModel.changesList)
        binding.rvVaccinationList.adapter = MissedVaccinationAdapter(list)

        val missedItems = viewModel.changesList.filter { it.vaccinatedDate == null }.map { it.vaccineName }
        binding.tvNoOfMissedVaccine.text = getString(R.string.missed_vaccination_count, missedItems.size)
        binding.labelMissedVaccinationTitle.text = getString(R.string.reason_for_missed_vaccination, missedItems.joinToString(", "))
        binding.labelMissedVaccinationTitle.markMandatory()

        binding.etMissedReason.addTextChangedListener {
            val reason = it?.trim().toString()
            binding.btnNext.isEnabled = reason.isNotEmpty()
        }
    }

    private fun clickListener() {
        binding.btnNext.isEnabled = false
        binding.btnBack.setOnClickListener {
            dismiss()
            viewModel.setUserJourney(AnalyticsDefinedParams.BackButtonClicked)
        }

        binding.ivClose.setOnClickListener {
            dismiss()
            viewModel.setUserJourney(AnalyticsDefinedParams.CLOSEICONTRIGGERED)
        }

        binding.btnNext.setOnClickListener {
            dismiss()
            val reason = binding.etMissedReason.text
                ?.trim()
                .toString()
            viewModel.shouldShowMissedVaccinationDialog(false, reason)
            viewModel.setUserJourney(AnalyticsDefinedParams.NEXTBUTTONTRIGGERED)
        }
    }
}
