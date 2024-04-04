package com.medtroniclabs.spice.ui.medicalreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.databinding.FragmentClinicalNotesBinding
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.AboveFiveYearsViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClinicalNotesFragment : BaseFragment() {

    private lateinit var binding: FragmentClinicalNotesBinding
    private val viewModel : AboveFiveYearsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentClinicalNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
    }

    private fun initializeViews() {
        binding.etClinicalNotes.addTextChangedListener {
            it?.let {
                viewModel.enteredClinicalNotes = it.toString()
                setFragmentResult(
                    MedicalReviewDefinedParams.CLINICAL_NOTES, bundleOf(
                        MedicalReviewDefinedParams.Notes to true)
                )
            }
        }
    }

}