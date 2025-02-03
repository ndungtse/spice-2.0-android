package com.medtroniclabs.spice.ui.medicalreview.tb.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.medtroniclabs.spice.databinding.FragmentPresumptiveTreatmentAndHistoryBinding
import com.medtroniclabs.spice.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PresumptiveTreatmentAndHistoryFragment : BaseFragment() {
    private lateinit var binding: FragmentPresumptiveTreatmentAndHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPresumptiveTreatmentAndHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "PresumptiveTreatmentAndHistoryFragment"
        fun newInstance() =
            PresumptiveTreatmentAndHistoryFragment()
    }
}