package com.medtroniclabs.spice.ui.medicalreview.epi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.getDisplayDimensions
import com.medtroniclabs.spice.databinding.DialogEpiCatchUpPolicyBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseDialogFragment
import com.medtroniclabs.spice.ui.medicalreview.epi.adapter.EpiCatchUpPolicyAdapter
import com.medtroniclabs.spice.ui.medicalreview.epi.viewmodel.ImmunisationViewModel

class EpiCatchUpPolicyDialogFragment  : BaseDialogFragment() {

    private val viewModel: ImmunisationViewModel by activityViewModels()
    private lateinit var binding: DialogEpiCatchUpPolicyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogEpiCatchUpPolicyBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_rect_white)
        isCancelable = false
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val (targetWidth, targetHeight) = getDisplayDimensions(requireActivity(), 0.85, 0.8)
        dialog?.window?.setLayout(targetWidth, targetHeight)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        clickListener()
    }

    private fun initView() {
        viewModel.getEpiCatchUpPolicyItems()
        viewModel.epiCatchUpPolicyItems.observe(viewLifecycleOwner) {
            when(it.state) {
                ResourceState.SUCCESS -> {
                    it.data?.let { list ->
                        binding.rvCatchPolicyList.adapter = EpiCatchUpPolicyAdapter(list)
                    }
                }
                else -> {

                }
            }
        }
    }

    private fun clickListener() {
        binding.btnDone.safeClickListener {
            dismiss()
        }

        binding.ivClose.safeClickListener {
            dismiss()
        }
    }
}