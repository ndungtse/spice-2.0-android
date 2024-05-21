package com.medtroniclabs.spice.ui.mypatients.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentPatientSearchFilterDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientListViewModel
import timber.log.Timber

class PatientSearchFilterDialog : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentPatientSearchFilterDialogBinding
    private lateinit var medicalReviewDueTag: TagListCustomView
    private lateinit var patientStatusTag: TagListCustomView
    private val patientListViewModel: PatientListViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPatientSearchFilterDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        medicalReviewDueTag =
            TagListCustomView(binding.root.context, binding.medicalReviewDueChipGroup) { _, _, _ ->
                patientListViewModel.medicalReviewDueTag = medicalReviewDueTag.getSelectedTags().takeIf { it.isNotEmpty() }
                enableConfirm()
            }
        patientStatusTag =
            TagListCustomView(binding.root.context, binding.patientStatusChipGroup) { _, _, _ ->
                patientListViewModel.patientStatusTag = patientStatusTag.getSelectedTags().takeIf { it.isNotEmpty() }
                enableConfirm()
            }
        medicalReviewDueTag.addChipItemList(
            getMedicalReviewDueChip(),
            patientListViewModel.medicalReviewDueTag
        )
        patientStatusTag.addChipItemList(
            getPatientStatusChip(),
            patientListViewModel.patientStatusTag
        )
        binding.btnLayout.btnCancel.safeClickListener(this)
        binding.imgClose.safeClickListener(this)
        binding.btnLayout.btnConfirm.safeClickListener(this)
    }

    private fun enableConfirm() {
        binding.btnLayout.btnConfirm.isEnabled =
            patientListViewModel.patientStatusTag?.isNotEmpty() == true || patientListViewModel.medicalReviewDueTag?.isNotEmpty() == true
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
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

    companion object {
        const val TAG = "PatientSearchFilterDialog"
        fun newInstance(): PatientSearchFilterDialog {
            return PatientSearchFilterDialog()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imgClose.id -> dismiss()
            binding.btnLayout.btnCancel.id -> {
                patientListViewModel.patientStatusTag = null
                patientListViewModel.medicalReviewDueTag = null
                patientListViewModel.setFilter(true)
                dismiss()
            }

            binding.btnLayout.btnConfirm.id -> {
                patientListViewModel.setFilter(true)
                dismiss()
            }
        }
    }

    fun getMedicalReviewDueChip(): ArrayList<ChipViewItemModel> {
        val chipItemList = ArrayList<ChipViewItemModel>()
        chipItemList.add(
            ChipViewItemModel(
                id = 1,
                name = getString(R.string.today)
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 2,
                name = getString(R.string.tomorrow)
            )
        )
        return chipItemList
    }

    fun getPatientStatusChip(): ArrayList<ChipViewItemModel> {
        val chipItemList = ArrayList<ChipViewItemModel>()
        chipItemList.add(
            ChipViewItemModel(
                id = 1,
                name = getString(R.string.referred)
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 2,
                name = getString(R.string.on_treatment)
            )
        )
        return chipItemList
    }

}