package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.medtroniclabs.spice.databinding.FragmentHivSummaryBinding
import com.medtroniclabs.spice.ui.BaseFragment

class HivSummaryFragment : BaseFragment() {
    private lateinit var binding: FragmentHivSummaryBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHivSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }
    companion object {
        const val TAG = "HivSummaryFragment"
        fun newInstance(): HivSummaryFragment {
            val args = Bundle()
            val fragment = HivSummaryFragment()
            fragment.arguments = args
            return fragment
        }
    }
}