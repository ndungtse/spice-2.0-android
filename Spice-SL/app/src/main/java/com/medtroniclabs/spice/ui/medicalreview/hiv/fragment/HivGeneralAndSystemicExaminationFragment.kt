package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentHivGeneralAndSystemicExaminationBinding
import com.medtroniclabs.spice.databinding.HivGeneralSystemicItemLayoutBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivGeneralAndSystemicExaminationViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HivGeneralAndSystemicExaminationFragment : BaseFragment() {

    private lateinit var binding: FragmentHivGeneralAndSystemicExaminationBinding
    private lateinit var examinationsTagView: TagListCustomView
    private val viewModel: HivGeneralAndSystemicExaminationViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.systemicExaminationsType =
                it.getString(MedicalReviewTypeEnums.SystemicExaminations.name) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentHivGeneralAndSystemicExaminationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        attachObserver()
        setListener()
    }

    companion object {

        const val TAG = "HivGeneralAndSystemicExaminationFragment"

        fun newInstance() =
            HivGeneralAndSystemicExaminationFragment()
    }

    fun initializeViews() {
        initTag()
    }

    private fun initTag() {
        examinationsTagView =
            TagListCustomView(
                binding.root.context,
                binding.tagSystemicExaminationTitle
            ) { name, _, isChecked ->
                viewModel.selectedSystemicExaminations =
                    ArrayList(examinationsTagView.getSelectedTags())
                if (!isChecked) {
                    viewModel.resultHashMap.remove(name)
                }
                showListView(ArrayList(examinationsTagView.getSelectedTags()).map { it.name })
                setFragmentResult(
                    MedicalReviewDefinedParams.SE_ITEM, bundleOf(
                        MedicalReviewDefinedParams.CHIP_ITEMS to true
                    )
                )
            }
        viewModel.getSystemicExaminationList(viewModel.systemicExaminationsType)
    }

    private fun showListView(listItems: List<String>) {
        binding.llGeneralExamination.removeAllViews()
        listItems.forEach { name ->
            val trimmedName = name.trim()
            val tag = trimmedName + "Root"
            val bindingItem = HivGeneralSystemicItemLayoutBinding.inflate(layoutInflater).apply {
                root.tag = tag
                tvRespiratoryLabel.text = trimmedName
                // Prepopulate if existing
                val existingText = viewModel.resultHashMap[trimmedName]?.trim().orEmpty()
                if (existingText.isNotEmpty()) {
                    tvRespiratoryText.setText(existingText)
                }
                MotherNeonateUtil.initTextWatcherForString(tvRespiratoryText) {
                    if (it.isBlank()) {
                        viewModel.resultHashMap.remove(trimmedName)
                    } else {
                        viewModel.resultHashMap[trimmedName] = it
                    }
                }
            }
            binding.llGeneralExamination.addView(bindingItem.root)
        }
    }


    private fun attachObserver() {
        viewModel.systemicExaminationList.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let { listItems ->
                        val chipItemList = ArrayList<ChipViewItemModel>()
                        listItems.filter { it.category == MedicalReviewTypeEnums.SystemicExaminations.name }
                            .forEach {
                                chipItemList.add(
                                    ChipViewItemModel(
                                        id = it.id,
                                        name = it.name,
                                        value = it.value
                                    )
                                )
                            }
                        examinationsTagView.addChipItemList(
                            chipItemList,
                            viewModel.selectedSystemicExaminations
                        )
                    }
                    hideProgress()
                }

                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    fun setListener() {

    }
}