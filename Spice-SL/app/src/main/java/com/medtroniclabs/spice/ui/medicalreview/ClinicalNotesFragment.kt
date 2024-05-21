package com.medtroniclabs.spice.ui.medicalreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.databinding.FragmentClinicalNotesBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClinicalNotesFragment : BaseFragment() {

    private lateinit var binding: FragmentClinicalNotesBinding
    private val viewModel : ClinicalNotesViewModel by activityViewModels()

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
        binding.tvClinicalNotesTitle.markMandatory()
        binding.etClinicalNotes.addTextChangedListener {
            it?.let {
                viewModel.enteredClinicalNotes = it.toString()
                viewModel.handleSubmitButtonState()
                setFragmentResult(
                    MedicalReviewDefinedParams.CLINICAL_NOTES, bundleOf(
                        MedicalReviewDefinedParams.Notes to true)
                )
            }
        }
    }

    fun validateInput():Boolean {
        if (binding.etClinicalNotes.text?.trim().toString().isBlank()) {
            binding.tvClinicalNoteErrorMessage.visible()
            binding.etClinicalNotes.requestFocus()
            return false
        }
        binding.tvClinicalNoteErrorMessage.gone()
        return true
    }
}