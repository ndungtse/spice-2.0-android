package com.medtroniclabs.spice.ui.medicalreview.undertwomonths

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.medtroniclabs.spice.databinding.FragmentBirthHistoryBinding

class BirthHistoryFragment : Fragment() {
    private lateinit var binding: FragmentBirthHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentBirthHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}