package com.medtroniclabs.spice.ui.medicalreview.epi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.DialogMissedVaccinationBinding
import com.medtroniclabs.spice.databinding.DialogUpdateVaccinationStatusBinding
import com.medtroniclabs.spice.model.medicalreview.VaccinationDetail
import com.medtroniclabs.spice.ui.BaseDialogFragment
import com.medtroniclabs.spice.ui.medicalreview.epi.adapter.MissedVaccinationAdapter
import com.medtroniclabs.spice.ui.medicalreview.epi.viewmodel.ImmunisationViewModel

class MissedImmunisationDialogFragment : BaseDialogFragment() {

    private lateinit var binding: DialogMissedVaccinationBinding
    private val viewModel: ImmunisationViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogMissedVaccinationBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_rect_white)
        isCancelable = false
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (600 * resources.displayMetrics.density).toInt(),
            (700 * resources.displayMetrics.density).toInt(),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        clickListener()
    }

    private fun initView() {
        val list = mutableListOf<VaccinationDetail>()
        //Adding dummy object for header
        list.add(VaccinationDetail(type = "Week", vaccineName = "", scheduledDate = "", doseClosureWeeks = "", displayOrder = 0))
        list.addAll(viewModel.changesList)
        binding.rvVaccinationList.adapter = MissedVaccinationAdapter(list)
    }

    private fun clickListener() {
        binding.btnBack.setOnClickListener {
            dismiss()
        }

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btnNext.setOnClickListener {
            dismiss()
            val reason = binding.etMissedReason.text?.trim().toString()
            viewModel.shouldShowMissedVaccinationDialog(false, reason)
        }
    }

}