package com.medtroniclabs.spice.ui.medicalreview.tb.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.medtroniclabs.spice.databinding.FragmentTbSummaryBinding
import com.medtroniclabs.spice.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class TbSummaryFragment : BaseFragment() {

    private lateinit var binding: FragmentTbSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTbSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "TbSummaryFragment"
        fun newInstance() =
            TbSummaryFragment()
    }
}