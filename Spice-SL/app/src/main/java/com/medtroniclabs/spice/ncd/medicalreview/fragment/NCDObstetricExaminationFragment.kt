package com.medtroniclabs.spice.ncd.medicalreview.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentSystemicExaminationsBinding
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.MENU_ID
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDObstetricExaminationViewModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil

class NCDObstetricExaminationFragment : BaseFragment() {

    companion object {
        const val TAG = "NCDObstetricExaminationFragment"
        fun newInstance(menuId: String?): NCDObstetricExaminationFragment {
            return NCDObstetricExaminationFragment().apply {
                arguments = Bundle().apply {
                    putString(MENU_ID, menuId)
                }
            }
        }
    }

    fun getType(): String? {
        return arguments?.getString(MENU_ID)
    }

    private val viewModel: NCDObstetricExaminationViewModel by activityViewModels()
    private lateinit var binding: FragmentSystemicExaminationsBinding
    private lateinit var tagView: TagListCustomView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSystemicExaminationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getChips(getType())
        setObserver()
        attachObserver()
        MotherNeonateUtil.initTextWatcherForString(binding.etPhysicalExaminationComments) {
            viewModel.comments = it
        }
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
        /*never used
        * */
    }


    private fun initView(
        complaintList: ArrayList<ChipViewItemModel>
    ) {
        with(binding) {
            binding.etPhysicalExaminationComments.visible()
            binding.tvCommentsTitle.gone()
            tvSystemicExaminationTitle.text = getString(R.string.obstetric_examination)
            tagPhysicalExamination.isSingleSelection = true
            tagView =
                TagListCustomView(
                    root.context,
                    tagPhysicalExamination,
                    callBack = { _, _, _ ->
                        viewModel.chips.clear()
                        viewModel.chips =
                            ArrayList(tagView.getSelectedTags())
                    }
                )
            tagView.addChipItemList(complaintList, viewModel.chips)
        }
    }

    fun validateInput(isMandatory: Boolean = false): Boolean {
        val hasChips = viewModel.chips.isNotEmpty() // Check if there are any chips selected
        val commentsNotBlank =
            binding.etPhysicalExaminationComments.text?.isNotBlank() == true // Check if the comments are not blank

        // If input is mandatory, additional validation is required
        if (isMandatory) {
            // If there are chips, we need to check further
            if (hasChips || commentsNotBlank) {
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
        if (hasChips || commentsNotBlank) {
            binding.tvErrorMessage.gone()
            return true
        }

        return true // If no other conditions matched, input is considered valid
    }

}