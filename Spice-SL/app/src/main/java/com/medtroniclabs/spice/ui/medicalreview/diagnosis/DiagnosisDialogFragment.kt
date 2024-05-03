package com.medtroniclabs.spice.ui.medicalreview.diagnosis

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentDiagnosisDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel.DiagnosisViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiagnosisDialogFragment : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentDiagnosisDialogBinding
    private lateinit var diseaseCategoryTagView: TagListCustomView
    private lateinit var diseaseConditionTagView: TagListCustomView
    private val diagnosisViewModel: DiagnosisViewModel by activityViewModels()

    companion object {
        const val TAG: String = "DiagnosisDialogueFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiagnosisDialogBinding.inflate(layoutInflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListener()
        attachObserver()
    }

    private fun setListener() {
        binding.btnCancel.safeClickListener(this)
        binding.ivClose.safeClickListener(this)
    }

    private fun attachObserver() {
        diagnosisViewModel.diagnosisList.observe(viewLifecycleOwner) { resource ->
            when (resource.state) {
                ResourceState.SUCCESS -> {
                    resource.data?.let { listItems ->
                        val chipItemList = ArrayList<ChipViewItemModel>()
                        listItems.forEach {
                            chipItemList.add(
                                ChipViewItemModel(
                                    id = it.id, name = it.name
                                )
                            )
                        }
                        diseaseCategoryTagView.addChipItemList(chipItemList, null)
                    }
                }

                else -> {

                }
            }
        }
    }

    private fun initView() {
        diagnosisViewModel.getDiagnosisList()
        diseaseCategoryTagView = TagListCustomView(
            binding.root.context, binding.diseaseCategoryChipGroup
        ) { name, _, _ ->
            validateDiseaseCategories(name)
            showSelectedDiseaseCategory(name)
            diagnosisViewModel.selectedDiseaseCategories =
                ArrayList(diseaseCategoryTagView.getSelectedTags())
        }
        diseaseConditionTagView = TagListCustomView(binding.root.context,
            binding.diseaseConditionChipGroup,
            otherCallBack = { name, isChecked ->
                showSelectedDiseaseConditions()
            }) { _, _, _ ->
        }
    }

    private fun validateDiseaseCategories(name: String?) {
        name?.let {
            if (diagnosisViewModel.selectedDiseaseCategories.any { it.name.lowercase() == name.lowercase() }) {
                val getDiseaseConditionByCategory =
                    diagnosisViewModel.diagnosisList.value?.data?.find { it.name.lowercase() == name.lowercase() }?.id
                getDiseaseConditionByCategory?.let { itemId ->
                        diagnosisViewModel.selectedDiseaseConditions.removeAll { it.diseaseId == itemId }
                }
            }
        }
    }

    private fun showSelectedDiseaseConditions() {
        val selectedTags = diseaseConditionTagView.getSelectedTags()
        if (selectedTags.isNotEmpty()) {
            diagnosisViewModel.selectedDiseaseConditions.addAll(selectedTags)
        }
    }

    private fun showSelectedDiseaseCategory(name: String?) {
        name?.let {
            binding.diseaseConditionViewGroup.visible()
            binding.tvSelectedDiseaseConditionLbl.visible()
            binding.tvSelectedDiseaseCondition.text = name
            diagnosisViewModel.selectedDiseaseCategoryName = name.lowercase()
            val diseaseConditionList =
                diagnosisViewModel.diagnosisList.value?.data?.let { list -> list.find { it.name.lowercase() == name.lowercase() } }
            val chipItemList = ArrayList<ChipViewItemModel>()
            diseaseConditionList?.diseaseCondition?.forEach {
                chipItemList.add(
                    ChipViewItemModel(
                        id = it.id, name = it.name, diseaseId = it.diseaseId
                    )
                )
            }
            diseaseConditionTagView.addChipItemList(
                chipItemList, diagnosisViewModel.selectedDiseaseConditions
            )
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnCancel.id, binding.ivClose.id -> {
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    private fun handleDialogSize() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = if (CommonUtils.checkIsTablet(requireContext())) {
            if (isLandscape) 65 else 90
        } else {
            if (isLandscape) 65 else 90
        }
        setWidth(width)
    }
}