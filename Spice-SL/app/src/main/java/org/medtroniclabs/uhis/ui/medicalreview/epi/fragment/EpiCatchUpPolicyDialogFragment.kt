package org.medtroniclabs.uhis.ui.medicalreview.epi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.getDisplayDimensions
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.databinding.DialogEpiCatchUpPolicyBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseDialogFragment
import org.medtroniclabs.uhis.ui.medicalreview.epi.adapter.EpiCatchUpPolicyAdapter
import org.medtroniclabs.uhis.ui.medicalreview.epi.viewmodel.ImmunisationViewModel

class EpiCatchUpPolicyDialogFragment(private val missedVaccineCount: Int) : BaseDialogFragment() {
    private val viewModel: ImmunisationViewModel by activityViewModels()
    private lateinit var binding: DialogEpiCatchUpPolicyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        clickListener()
        viewModel.setUserJourney(AnalyticsDefinedParams.EpiCatchUpPolicyDialog)
    }

    private fun initView() {
        if (missedVaccineCount > 0) {
            binding.tvNoOfMissedVaccine.visible()
            binding.tvNoOfMissedVaccine.text = getString(R.string.missed_vaccination_count, missedVaccineCount)
        } else {
            binding.tvNoOfMissedVaccine.gone()
        }

        viewModel.getEpiCatchUpPolicyItems()
        viewModel.epiCatchUpPolicyItems.observe(viewLifecycleOwner) {
            when (it.state) {
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
            viewModel.setUserJourney(AnalyticsDefinedParams.OKAYBUTTONTRIGGERED)
        }

        binding.ivClose.safeClickListener {
            dismiss()
            viewModel.setUserJourney(AnalyticsDefinedParams.CLOSEICONTRIGGERED)
        }
    }
}
