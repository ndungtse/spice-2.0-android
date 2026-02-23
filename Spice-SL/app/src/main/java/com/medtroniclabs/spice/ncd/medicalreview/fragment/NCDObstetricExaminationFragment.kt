package com.medtroniclabs.spice.ncd.medicalreview.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentSystemicExaminationsBinding
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.MENU_ID
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDObstetricExaminationViewModel
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil

class NCDObstetricExaminationFragment : BaseFragment() {
    companion object {
        const val TAG = "NCDObstetricExaminationFragment"
        const val IS_FEMALE_PREGNANT = "isFemalePregnant"

        fun newInstance(
            menuId: String?,
            isFemalePregnant: Boolean,
        ): NCDObstetricExaminationFragment =
            NCDObstetricExaminationFragment().apply {
                arguments = Bundle().apply {
                    putString(MENU_ID, menuId)
                    putBoolean(IS_FEMALE_PREGNANT, isFemalePregnant)
                }
            }
    }

    fun getType(): String? = arguments?.getString(MENU_ID)

    private fun isFemalePregnant(): Boolean = arguments?.getBoolean(IS_FEMALE_PREGNANT) == true

    private val viewModel: NCDObstetricExaminationViewModel by activityViewModels()
    private lateinit var binding: FragmentSystemicExaminationsBinding
    private lateinit var tagView: TagListCustomView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSystemicExaminationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getChips(handleChipType(getType(), isFemalePregnant()))
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
                    cultureValue = item.displayValue,
                    type = item.type,
                    value = item.value,
                )
            } as ArrayList<ChipViewItemModel>
            initView(complaintList)
        }
    }

    private fun setObserver() {
        /*never used
         * */
    }

    private fun initView(complaintList: ArrayList<ChipViewItemModel>) {
        with(binding) {
            binding.etPhysicalExaminationComments.visible()
            binding.tvCommentsTitle.gone()
            getType()?.let { type ->
                tvSystemicExaminationTitle.text = CommonUtils.getPhysicalExaminationTitle(
                    requireContext(),
                    type,
                    isFemalePregnant(),
                )
            }
            tagView =
                TagListCustomView(
                    root.context,
                    tagPhysicalExamination,
                    callBack = { _, _, _ ->
                        viewModel.chips.clear()
                        viewModel.chips =
                            ArrayList(tagView.getSelectedTags())
                    },
                )
            tagView.addChipItemList(complaintList, viewModel.chips)
        }
    }

    fun validateInput(isMandatory: Boolean = false): Pair<Boolean, AppCompatEditText> =
        Pair(
            NCDMRUtil.validateInputForCommentOption(
                isMandatory,
                viewModel.chips,
                binding.etPhysicalExaminationComments,
                binding.tvErrorMessage,
            ),
            binding.etPhysicalExaminationComments,
        )
}
