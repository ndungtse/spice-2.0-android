package com.medtroniclabs.spice.ui.medicalreview.tb.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.TB
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentSystemicExaminationsBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel.ComorbiditiesViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComorbiditiesFragment : BaseFragment() {

    private val viewModel: ComorbiditiesViewModel by activityViewModels()
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
        const val TAG = "ComorbiditiesFragment"
        fun newInstance() = ComorbiditiesFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getChips(TB)
        setObserver()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.getChipItems.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let { listItems ->
                        val chipItemList =
                            listItems.filter { it.category == MedicalReviewTypeEnums.comorbidities.name }
                                .map {
                                    ChipViewItemModel(
                                        id = it.id,
                                        name = it.name,
                                        value = it.value
                                    )
                                } as ArrayList<ChipViewItemModel>
                        initView(chipItemList)
                    }
                    hideProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun setObserver() {
        MotherNeonateUtil.initTextWatcherForString(binding.etPhysicalExaminationComments) {
            viewModel.comments = it
            setFragmentResult(
                MedicalReviewDefinedParams.CF_ITEM, bundleOf(
                    MedicalReviewDefinedParams.CHIP_ITEMS to true)
            )
        }
    }


    private fun initView(
        complaintList: ArrayList<ChipViewItemModel>
    ) {
        with(binding) {
            binding.tvCommentsTitle.text = getString(R.string.please_specify_the_comorbidity)
            binding.tvCommentsTitle.markMandatory()
            binding.tvCommentsTitle.gone()
            binding.etPhysicalExaminationComments.gone()
            binding.tvCommentsTitle.gone()
            tvSystemicExaminationTitle.text = getString(R.string.comorbidities)
            tagView =
                TagListCustomView(
                    root.context,
                    tagPhysicalExamination,
                    callBack = { _, _, _ ->
                        viewModel.chips.clear()
                        viewModel.chips =
                            ArrayList(tagView.getSelectedTags())
                        showNotes()
                        setFragmentResult(
                            MedicalReviewDefinedParams.CF_ITEM, bundleOf(
                                MedicalReviewDefinedParams.CHIP_ITEMS to true)
                        )
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
            binding.tvCommentsTitle.gone()
        } else {
            binding.etPhysicalExaminationComments.gone()
            binding.tvCommentsTitle.gone()
            viewModel.comments = ""
            binding.etPhysicalExaminationComments.setText(getText(R.string.empty))
            binding.tvErrorMessage.gone()
        }
    }

    fun validateInput(isMandatory: Boolean = false): Pair<Boolean, AppCompatEditText> {
        val isValid = NCDMRUtil.validateInput(
            isMandatory,
            viewModel.chips,
            binding.etPhysicalExaminationComments
        )
        return Pair(isValid, binding.etPhysicalExaminationComments)
    }
}