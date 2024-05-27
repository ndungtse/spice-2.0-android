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
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentSystemicExaminationsBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.SystemicExaminationViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil.isDataValid
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SystemicExaminationsFragment : BaseFragment() {

    private lateinit var binding: FragmentSystemicExaminationsBinding
    private lateinit var examinationsTagView: TagListCustomView
    private val viewModel: SystemicExaminationViewModel by activityViewModels()

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
                viewModel.fundalHeight =  if (value.isNotBlank()) value.toDoubleOrNull() else null
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
                            if (viewModel.systemicExaminationsType == ANC.uppercase()) MedicalReviewTypeEnums.ObstetricExaminations.name else MedicalReviewTypeEnums.SystemicExaminations.name
                        listItems.filter { it.category == category }.forEach {
                            chipItemList.add(
                                ChipViewItemModel(
                                    id = it.id,
                                    name = it.name,
                                    value = it.value
                                )
                            )
                        }
                        examinationsTagView.addChipItemList(chipItemList, null)
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
            MedicalReviewTypeEnums.ANC.name -> Pair(R.string.obstetric_examination, true)
            MedicalReviewTypeEnums.AboveFiveYears.name,MedicalReviewTypeEnums.UnderFiveYears.name  -> Pair(
                R.string.systemic_examinations,
                false
            )

            else -> return // Handle other cases or provide a default behavior
        }

        with(binding) {
            tvSystemicExaminationTitle.text = getString(titleResId)
            obstetricGroup.visibility = if (showObstetricGroup) View.VISIBLE else View.GONE
            tvFundalHeightError.gone()
            tvFetalHeartRateError.gone()
        }
        initTag()
    }

    private fun initTag() {
        examinationsTagView =
            TagListCustomView(binding.root.context, binding.tagPhysicalExamination) { _, _, _ ->
                viewModel.selectedSystemicExaminations =
                    ArrayList(examinationsTagView.getSelectedTags())
                setFragmentResult(
                    MedicalReviewDefinedParams.SE_ITEM, bundleOf(
                        MedicalReviewDefinedParams.CHIP_ITEMS to true
                    )
                )
            }
        viewModel.getSystemicExaminationList(viewModel.systemicExaminationsType)
    }

    fun validateInput(): Boolean {
        val isFundalHeightValid = isDataValid(viewModel.fundalHeight, binding.tvFundalHeightError)
        val isFetalHeartRateValid =
            isDataValid(viewModel.fetalHeartRate, binding.tvFetalHeartRateError)
        return isFundalHeightValid && isFetalHeartRateValid
    }

}