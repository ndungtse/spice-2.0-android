package com.medtroniclabs.spice.ui.medicalreview.motherneonate.labourdelivery.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.model.NeonateDTO
import com.medtroniclabs.spice.databinding.FragmentNeonateSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.labourdelivery.viewmodel.LabourDeliverySummaryViewModel

class NeonateSummaryFragment : BaseFragment() {

    private lateinit var binding: FragmentNeonateSummaryBinding
    private val viewModel: LabourDeliverySummaryViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNeonateSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.summaryDetailsLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { neonateState ->
                        setDetails(neonateState.neonateDTO)
                    }
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun setDetails(neonateDTO: NeonateDTO?) {
        binding.tvNeonateOutcome.text =
            neonateDTO?.neonateOutcome ?: getString(R.string.hyphen_symbol)
        binding.tvGender.text =
            neonateDTO?.gender?.capitalizeFirstChar() ?: getString(R.string.hyphen_symbol)
        binding.tvBirthWeight.text = neonateDTO?.birthWeight ?: getString(R.string.hyphen_symbol)
        binding.tvStateOfBaby.text = neonateDTO?.stateOfBaby ?: getString(R.string.hyphen_symbol)
        binding.tvGestationalAge.text =
            neonateDTO?.gestationalAge ?: getString(R.string.hyphen_symbol)
        binding.tvAPGARScore.text =
            if (neonateDTO?.apgarScoreFiveMinuteDTO?.fiveMinuteTotalScore == null) {
                getString(R.string.hyphen_symbol)
            } else {
                neonateDTO.apgarScoreFiveMinuteDTO.fiveMinuteTotalScore.toString().plus(
                    getString(
                        R.string.five_minutes
                    )
                )
            }
        binding.tvSignsSymptomsObserved.text = neonateDTO?.signs?.map { it }
            ?.let { ArrayList(it) }?.let { CommonUtils.convertListToString(it) }?.split(", ")
            ?.joinToString("\n")
            ?: getString(R.string.hyphen_symbol)
        binding.tvPatientStatus.text = getString(R.string.title_neonate)
    }

    companion object {
        const val TAG = "NeonateSummaryFragment"

        fun newInstance(): NeonateSummaryFragment {
            return NeonateSummaryFragment()
        }
    }
}