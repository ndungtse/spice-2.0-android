package org.medtroniclabs.uhis.ncd.medicalreview.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.databinding.FragmentSystemicExaminationsBinding
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil
import org.medtroniclabs.uhis.ncd.medicalreview.NCDMRUtil.MENU_ID
import org.medtroniclabs.uhis.ncd.medicalreview.viewmodel.NCDChiefComplaintsViewModel
import org.medtroniclabs.uhis.ui.BaseFragment
import org.medtroniclabs.uhis.ui.TagListCustomView
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.MotherNeonateUtil

class NCDChiefComplaintsFragment : BaseFragment() {
    private val viewModel: NCDChiefComplaintsViewModel by activityViewModels()
    private lateinit var binding: FragmentSystemicExaminationsBinding
    private lateinit var tagView: TagListCustomView

    companion object {
        const val TAG = "NCDChiefComplaintsFragment"
        const val IS_FEMALE_PREGNANT = "isFemalePregnant"

        fun newInstance(
            menuId: String?,
            isFemalePregnant: Boolean,
        ): NCDChiefComplaintsFragment =
            NCDChiefComplaintsFragment().apply {
                arguments = Bundle().apply {
                    putString(MENU_ID, menuId)
                    putBoolean(IS_FEMALE_PREGNANT, isFemalePregnant)
                }
            }
    }

    fun getType(): String? = arguments?.getString(MENU_ID)

    private fun isFemalePregnant(): Boolean = arguments?.getBoolean(NCDObstetricExaminationFragment.IS_FEMALE_PREGNANT) == true

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
        /*Never used
         * */
    }

    private fun initView(complaintList: ArrayList<ChipViewItemModel>) {
        with(binding) {
            binding.etPhysicalExaminationComments.visible()
            binding.tvCommentsTitle.gone()
            tvSystemicExaminationTitle.text = getString(R.string.chief_complaints)
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
