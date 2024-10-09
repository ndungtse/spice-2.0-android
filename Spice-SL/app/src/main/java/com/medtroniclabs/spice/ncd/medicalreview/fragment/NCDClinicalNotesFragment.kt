package com.medtroniclabs.spice.ncd.medicalreview.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.databinding.FragmentSystemicExaminationsBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDClinicalNotesViewModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil

class NCDClinicalNotesFragment : BaseFragment() {

    private val viewModel: NCDClinicalNotesViewModel by activityViewModels()
    private lateinit var binding: FragmentSystemicExaminationsBinding

    companion object {
        const val TAG = "ClinicalNotesFragment"
        fun newInstance() =
            NCDClinicalNotesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSystemicExaminationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
        attachObserver()
        initView()
    }

    private fun attachObserver() {
    }

    private fun setObserver() {
        /*never Used
        * */
    }


    private fun initView() {
        with(binding) {
            etPhysicalExaminationComments.visible()
            tvCommentsTitle.gone()
            tagPhysicalExamination.gone()
            tvSystemicExaminationTitle.text = getString(R.string.clinical_notes)
            tvSystemicExaminationTitle.markMandatory()
            MotherNeonateUtil.initTextWatcherForString(binding.etPhysicalExaminationComments) {
                viewModel.comments = it
            }
        }
    }

    fun validateInput(isMandatory: Boolean = true): Pair<Boolean, AppCompatEditText> {
        val commentsNotBlank =
            binding.etPhysicalExaminationComments.text?.isNotBlank() == true // Check if the comments are not blank

        // If input is mandatory, additional validation is required
        if (isMandatory) {
            if (commentsNotBlank) {
                binding.tvErrorMessage.gone()
                return Pair(true, binding.etPhysicalExaminationComments)
            } else {
                binding.tvErrorMessage.visible()
                return Pair(false, binding.etPhysicalExaminationComments)
            }
        }

        if (!commentsNotBlank) {
            return Pair(true, binding.etPhysicalExaminationComments)
        }

        return Pair(
            true,
            binding.etPhysicalExaminationComments
        ) // If no other conditions matched, input is considered valid
    }
}