package com.medtroniclabs.spice.ui.mypatients.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.FragmentNeonateSummaryBinding
import com.medtroniclabs.spice.model.medicalreview.NeonateDTO
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.labourDelivery.LabourDeliveryViewModel

class NeonateSummaryFragment : BaseFragment() {

    private lateinit var binding: FragmentNeonateSummaryBinding
    private val viewModel: LabourDeliveryViewModel by activityViewModels()
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

    private fun setDetails(NeonateDTO: NeonateDTO?) {
        binding.tvNeonateOutcome.text = NeonateDTO?.neonateOutcome.toString()
        binding.tvGender.text = NeonateDTO?.gender.toString()
        binding.tvBirthWeight.text = NeonateDTO?.birthWeight.toString()
        binding.tvStateOfBaby.text = NeonateDTO?.stateOfBaby.toString()
        binding.tvGestationalAge.text = NeonateDTO?.gestationalAge.toString()
        binding.tvAPGARScore.text = NeonateDTO?.total.toString()
        binding.tvSignsSymptomsObserved.text = NeonateDTO?.signs?.map { it }
            ?.let { ArrayList(it) }?.let { CommonUtils.convertListToString(it) }
        binding.tvPatientStatus.text = ""
    }

    companion object {
        const val TAG = "NeonateSummaryFragment"

        fun newInstance(): NeonateSummaryFragment {
            return NeonateSummaryFragment()
        }
    }
}