package com.medtroniclabs.spice.ui.medicalreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentSystemicExaminationsBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.SystemicExaminationViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.isDataValid
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.respiratory
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SystemicExaminationsFragment : BaseFragment() {

    private lateinit var binding: FragmentSystemicExaminationsBinding
    private lateinit var examinationsTagView: TagListCustomView
    private val viewModel: SystemicExaminationViewModel by activityViewModels()

    companion object {
        const val TAG = "SystemicExaminationsFragment"
    }
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
        binding = FragmentSystemicExaminationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        attachObserver()
        setListener()
    }

    private fun setListener() {
        binding.etPhysicalExaminationComments.addTextChangedListener {
            it?.let {
                viewModel.enteredExaminationNotes = it.trim().toString()
                setFragmentResult(
                    MedicalReviewDefinedParams.SE_ITEM, bundleOf(
                        MedicalReviewDefinedParams.CHIP_ITEMS to true
                    )
                )
            }
        }
        binding.etFundalHeight.addTextChangedListener {
            it?.let {
                val value = it.trim().toString()
                viewModel.fundalHeight = if (value.isNotBlank()) value.toDoubleOrNull() else null
                setFragmentResult(
                    MedicalReviewDefinedParams.SE_ITEM, bundleOf(
                        MedicalReviewDefinedParams.CHIP_ITEMS to true
                    )
                )
            }
        }

        binding.tvRespiratoryText.addTextChangedListener {
            it?.let {
                val value = it.trim().toString()
                viewModel.respiratoryNotes = value.ifBlank { null }
                setFragmentResult(
                    MedicalReviewDefinedParams.SE_ITEM, bundleOf(
                        MedicalReviewDefinedParams.CHIP_ITEMS to true
                    )
                )
            }
        }

        binding.etFetalHeartRate.addTextChangedListener {
            it?.let {
                val value = it.trim().toString()
                viewModel.fetalHeartRate = if (value.isNotBlank()) value.toDoubleOrNull() else null
                setFragmentResult(
                    MedicalReviewDefinedParams.SE_ITEM, bundleOf(
                        MedicalReviewDefinedParams.CHIP_ITEMS to true
                    )
                )
            }
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
                        val category =
                            if (viewModel.systemicExaminationsType == MedicalReviewTypeEnums.ANC_REVIEW.name) MedicalReviewTypeEnums.ObstetricExaminations.name else MedicalReviewTypeEnums.SystemicExaminations.name
                        listItems.filter { it.category == category }.forEach {
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

    private fun initializeViews() {
        val (titleResId, showObstetricGroup) = when (viewModel.systemicExaminationsType) {
            MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name -> {Pair(R.string.systemic_examinations, true)}
            MedicalReviewTypeEnums.ANC_REVIEW.name,MedicalReviewTypeEnums.HIV.name -> Pair(R.string.obstetric_examination, true)
            MedicalReviewTypeEnums.UNDER_FIVE_YEARS.name -> Pair(
                R.string.systemic_examinations,
                false
            )

            MedicalReviewTypeEnums.ABOVE_FIVE_YEARS.name -> Pair(
                R.string.systemic_examinations,
                false
            )
            MedicalReviewTypeEnums.TB.name,MedicalReviewTypeEnums.HIV.name -> {
                binding.etPhysicalExaminationComments.gone()
                Pair(R.string.general_systemic_examinations, false)
            }

            else -> return // Handle other cases or provide a default behavior
        }

        with(binding) {
            tvSystemicExaminationTitle.text = getString(titleResId)
            obstetricGroup.visibility = if (showObstetricGroup) View.VISIBLE else View.GONE
            tvFundalHeightError.gone()
            tvFetalHeartRateError.gone()
        }
        initTag()
        if (viewModel.enteredExaminationNotes.isNotBlank()) {
            binding.etPhysicalExaminationComments.setText(viewModel.enteredExaminationNotes)
        }
        binding.tvRespiratoryLabel.markMandatory()
    }

    private fun initTag() {
        examinationsTagView =
            TagListCustomView(binding.root.context, binding.tagPhysicalExamination) { _, _, _ ->
                viewModel.selectedSystemicExaminations =
                    ArrayList(examinationsTagView.getSelectedTags())
                showRespiratory()
                setFragmentResult(
                    MedicalReviewDefinedParams.SE_ITEM, bundleOf(
                        MedicalReviewDefinedParams.CHIP_ITEMS to true
                    )
                )
            }
        viewModel.getSystemicExaminationList(viewModel.systemicExaminationsType)
    }

    private fun showRespiratory() {
        viewModel.selectedSystemicExaminations.find { it.value == respiratory }?.let {
            if (viewModel.systemicExaminationsType == MedicalReviewTypeEnums.TB.name) {
                binding.tbGroup.visible()
                binding.tvErrorMessage.invisible()
            }
        } ?: kotlin.run {
            binding.tbGroup.gone()
            binding.tvErrorMessage.gone()
            binding.tvRespiratoryText.setText("")
        }
    }

    fun validateInput(): Boolean {
        val isFundalHeightValid =
            isDataValid(
                viewModel.fundalHeight,
                binding.tvFundalHeightError,
                50,
                binding.etFundalHeight,
                requireContext()
            )
        val isFetalHeartRateValid =
            isDataValid(
                viewModel.fetalHeartRate,
                binding.tvFetalHeartRateError,
                200,
                binding.etFetalHeartRate,
                requireContext()
            )
        return isFundalHeightValid && isFetalHeartRateValid
    }
    fun refreshFragment() {
        examinationsTagView.clearSelection()
        examinationsTagView.clearOtherChip()
        binding.etPhysicalExaminationComments.text?.clear()
    }

}