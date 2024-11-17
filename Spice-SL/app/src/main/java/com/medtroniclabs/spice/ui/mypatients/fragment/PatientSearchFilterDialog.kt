package com.medtroniclabs.spice.ui.mypatients.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.databinding.FragmentPatientSearchFilterDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.TagListCustomView
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientListViewModel

class PatientSearchFilterDialog : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentPatientSearchFilterDialogBinding
    private lateinit var medicalReviewDueTag: TagListCustomView
    private lateinit var patientStatusTag: TagListCustomView

    private lateinit var ncdReferredForTag: TagListCustomView
    private lateinit var ncdMedicalReviewDateTag: TagListCustomView
    private lateinit var ncdRedRiskTag: TagListCustomView
    private lateinit var ncdRegistrationTag: TagListCustomView
    private lateinit var ncdCvdRiskTag: TagListCustomView
    private lateinit var ncdAssessmentTag: TagListCustomView
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
        binding.apply {
            if (CommonUtils.isDispenseOrInvestigation(requireArguments().getString(ORIGIN))) {
                referredForGroup.visible()
            } else {
                medicalReviewDateGroup.visible()
                riskGroup.visible()
                registrationGroup.visible()
                cvdGroup.visible()
                assessmentGroup.visible()
            }
        }
        binding.btnLayout.btnConfirm.text = requireContext().getString(R.string.apply)
        medicalReviewDueTag =
            TagListCustomView(binding.root.context, binding.medicalReviewDueChipGroup) { _, _, _ ->
                enableConfirm()
            }
        patientStatusTag =
            TagListCustomView(binding.root.context, binding.patientStatusChipGroup) { _, _, _ ->
                enableConfirm()
            }
        ncdReferredForTag =
            TagListCustomView(binding.root.context, binding.ncdReferredForChipGroup) { _, _, _ ->
                enableConfirm()
            }
        ncdMedicalReviewDateTag =
            TagListCustomView(binding.root.context, binding.ncdMedicalReviewDateChipGroup) { _, _, _ ->
                enableConfirm()
            }
        ncdRedRiskTag =
            TagListCustomView(binding.root.context, binding.riskChipGroup) { _, _, _ ->
                enableConfirm()
            }
        ncdRegistrationTag =
            TagListCustomView(binding.root.context, binding.registrationChipGroup) { _, _, _ ->
                enableConfirm()
            }
        ncdCvdRiskTag =
            TagListCustomView(binding.root.context, binding.cvdChipGroup) { _, _, _ ->
                enableConfirm()
            }
        ncdAssessmentTag =
            TagListCustomView(binding.root.context, binding.assessmentDateChipGroup) { _, _, _ ->
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
        ncdReferredForTag.addChipItemList(
            getTodayTomorrowChip(),
            patientListViewModel.ncdReferredForTag
        )
        ncdMedicalReviewDateTag.addChipItemList(
            getTodayTomorrowChip(),
            patientListViewModel.ncdMedicalReviewDateTag
        )
        ncdRedRiskTag.addChipItemList(
            getRedRisks(),
            patientListViewModel.ncdRedRiskTag
        )
        ncdRegistrationTag.addChipItemList(
            getRegistrations(),
            patientListViewModel.ncdRegistrationTag
        )
        ncdCvdRiskTag.addChipItemList(
            getCvdRisks(),
            patientListViewModel.ncdCvdRiskTag
        )
        ncdAssessmentTag.addChipItemList(
            getTodayTomorrowChip(),
            patientListViewModel.ncdAssessmentTag
        )
        binding.btnLayout.btnCancel.safeClickListener(this)
        binding.imgClose.safeClickListener(this)
        binding.btnLayout.btnConfirm.safeClickListener(this)
    }

    private fun enableConfirm() {
        binding.btnLayout.btnConfirm.isEnabled =
            patientStatusTag.getSelectedTags().isNotEmpty() ||
                    medicalReviewDueTag.getSelectedTags().isNotEmpty() ||
                    ncdReferredForTag.getSelectedTags().isNotEmpty() ||
                    ncdMedicalReviewDateTag.getSelectedTags().isNotEmpty() ||
                    ncdRedRiskTag.getSelectedTags().isNotEmpty() ||
                    ncdRegistrationTag.getSelectedTags().isNotEmpty() ||
                    ncdCvdRiskTag.getSelectedTags().isNotEmpty() ||
                    ncdAssessmentTag.getSelectedTags().isNotEmpty()
    }

    override fun onStart() {
        super.onStart()
        if (CommonUtils.checkIsTablet(requireContext())) {
            setDialogPercent(60)
        } else {
            setDialogPercent(90)
        }
    }

    companion object {
        const val TAG = "PatientSearchFilterDialog"
        const val ORIGIN = "origin"

        fun newInstance(origin: String?): PatientSearchFilterDialog {
            val args = Bundle()
            args.putString(ORIGIN, origin)
            val fragment = PatientSearchFilterDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imgClose.id -> dismiss()
            binding.btnLayout.btnCancel.id -> {
                patientListViewModel.patientStatusTag = null
                patientListViewModel.medicalReviewDueTag = null
                patientListViewModel.ncdReferredForTag = null
                patientListViewModel.ncdMedicalReviewDateTag = null
                patientListViewModel.ncdRedRiskTag = null
                patientListViewModel.ncdRegistrationTag = null
                patientListViewModel.ncdCvdRiskTag = null
                patientListViewModel.ncdAssessmentTag = null
                patientListViewModel.setFilter(true)
                dismiss()
            }

            binding.btnLayout.btnConfirm.id -> {
                patientListViewModel.medicalReviewDueTag = medicalReviewDueTag.getSelectedTags().takeIf { it.isNotEmpty() }
                patientListViewModel.patientStatusTag = patientStatusTag.getSelectedTags().takeIf { it.isNotEmpty() }
                patientListViewModel.ncdReferredForTag = ncdReferredForTag.getSelectedTags().takeIf { it.isNotEmpty() }
                patientListViewModel.ncdMedicalReviewDateTag = ncdMedicalReviewDateTag.getSelectedTags().takeIf { it.isNotEmpty() }
                patientListViewModel.ncdRedRiskTag = ncdRedRiskTag.getSelectedTags().takeIf { it.isNotEmpty() }
                patientListViewModel.ncdRegistrationTag = ncdRegistrationTag.getSelectedTags().takeIf { it.isNotEmpty() }
                patientListViewModel.ncdCvdRiskTag = ncdCvdRiskTag.getSelectedTags().takeIf { it.isNotEmpty() }
                patientListViewModel.ncdAssessmentTag = ncdAssessmentTag.getSelectedTags().takeIf { it.isNotEmpty() }
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

    fun getTodayTomorrowChip(): ArrayList<ChipViewItemModel> {
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

    fun getRegistrations(): ArrayList<ChipViewItemModel> {
        val chipItemList = ArrayList<ChipViewItemModel>()
        chipItemList.add(
            ChipViewItemModel(
                id = 1,
                name = getString(R.string.registered),
                optionalData = getString(R.string.enrolled_data)
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 2,
                name = getString(R.string.not_registered),
                optionalData = getString(R.string.not_enrolled_data)
            )
        )
        return chipItemList
    }

    fun getCvdRisks(): ArrayList<ChipViewItemModel> {
        val chipItemList = ArrayList<ChipViewItemModel>()
        chipItemList.add(
            ChipViewItemModel(
                id = 1,
                name = getString(R.string.high)
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 2,
                name = getString(R.string.medium)
            )
        )
        chipItemList.add(
            ChipViewItemModel(
                id = 2,
                name = getString(R.string.low)
            )
        )
        return chipItemList
    }

    fun getRedRisks(): ArrayList<ChipViewItemModel> {
        val chipItemList = ArrayList<ChipViewItemModel>()
        chipItemList.add(
            ChipViewItemModel(
                id = 1,
                name = getString(R.string.red_risk)
            )
        )
        return chipItemList
    }
}