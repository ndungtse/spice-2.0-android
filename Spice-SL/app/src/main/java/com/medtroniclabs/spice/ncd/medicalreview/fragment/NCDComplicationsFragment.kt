package com.medtroniclabs.spice.ncd.medicalreview.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentSystemicExaminationsBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDComplicationsViewModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil

class NCDComplicationsFragment : BaseFragment() {
    private val viewModel: NCDComplicationsViewModel by activityViewModels()
    private lateinit var binding: FragmentSystemicExaminationsBinding
    private lateinit var tagView: TagListCustomView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSystemicExaminationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "NCDComplicationsFragment"
        fun newInstance(): NCDComplicationsFragment {
            return NCDComplicationsFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getChips()
        setObserver()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.getChipItems.observe(viewLifecycleOwner) {
            val complaintList = it.map { item ->
                ChipViewItemModel(
                    id = item.id,
                    name = item.name,
                    type = item.type,
                    value = item.value
                )
            } as ArrayList<ChipViewItemModel>
            initView(complaintList)
        }
    }

    private fun setObserver() {
        MotherNeonateUtil.initTextWatcherForString(binding.etPhysicalExaminationComments) {
            viewModel.comments = it
        }
    }


    private fun initView(
        complaintList: ArrayList<ChipViewItemModel>
    ) {
        with(binding) {
            binding.tvSystemicExaminationTitle.text = getString(R.string.complications)
            binding.tvCommentsTitle.text = getString(R.string.please_specify_the_complication)
            binding.tvCommentsTitle.markMandatory()
            binding.etPhysicalExaminationComments.gone()
            binding.tvCommentsTitle.gone()
            tvSystemicExaminationTitle.text = getString(R.string.comorbidities)
            tagPhysicalExamination.isSingleSelection = true
            tagView =
                TagListCustomView(
                    root.context,
                    tagPhysicalExamination,
                    callBack = { _, _, _ ->
                        viewModel.chips.clear()
                        viewModel.chips =
                            ArrayList(tagView.getSelectedTags())
                        showNotes()
                    }
                )
            tagView.addChipItemList(complaintList, viewModel.chips)
        }
    }

    private fun showNotes() {
        if (viewModel.chips.firstOrNull {
                it.name.equals(
                    DefinedParams.Other,
                    ignoreCase = true
                )
            } != null) {
            binding.etPhysicalExaminationComments.visible()
            binding.tvCommentsTitle.visible()
        } else {
            binding.etPhysicalExaminationComments.gone()
            binding.tvCommentsTitle.gone()
            binding.tvErrorMessage.gone()
        }
    }

    fun validateInput(isMandatory: Boolean = false): Boolean {
        val hasChips = viewModel.chips.isNotEmpty() // Check if there are any chips selected
        val hasOtherChip = viewModel.chips.any {
            it.name.equals(
                DefinedParams.Other,
                ignoreCase = true
            )
        } // Check if 'Other' chip is present
        val commentsNotBlank =
            binding.etPhysicalExaminationComments.text?.isNotBlank() == true // Check if the comments are not blank

        // If input is mandatory, additional validation is required
        if (isMandatory) {
            binding.tvSystemicExaminationTitle.markMandatory()
            // If there are chips, we need to check further
            if (hasChips) {
                // If 'Other' chip is selected, check for non-blank comments
                if (hasOtherChip) {
                    return if (commentsNotBlank) {
                        binding.tvErrorMessage.invisible() // Hide error if comments are valid
                        true
                    } else {
                        binding.tvErrorMessage.visible() // Show error if comments are empty
                        false
                    }
                }
                // If no 'Other' chip is selected, input is valid
                binding.tvErrorMessage.gone()
                return true
            } else {
                // If no chips are selected and mandatory, show error
                binding.tvErrorMessage.visible()
                return false
            }
        }

        // If chips are empty and comments are blank, input is valid
        if (!hasChips && binding.etPhysicalExaminationComments.text?.isBlank() == true) {
            binding.tvErrorMessage.gone()
            return true
        }

        // If chips are not empty, check for 'Other' chip and non-blank comments
        if (hasChips) {
            if (hasOtherChip) {
                return if (commentsNotBlank) {
                    binding.tvErrorMessage.invisible() // Hide error if comments are valid
                    true
                } else {
                    binding.tvErrorMessage.visible() // Show error if comments are empty
                    false
                }
            }
            // If no 'Other' chip is selected, input is valid
            binding.tvErrorMessage.gone()
            return true
        }

        return true // If no other conditions matched, input is considered valid
    }

}