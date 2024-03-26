package com.medtroniclabs.spice.ui.mypatients.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.medtroniclabs.spice.databinding.FragmentMedicalReviewPatientDiagnosisBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MedicalReviewPatientDiagnosisFragment : Fragment() {

    private lateinit var binding: FragmentMedicalReviewPatientDiagnosisBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMedicalReviewPatientDiagnosisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}