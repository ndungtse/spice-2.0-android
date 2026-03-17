package org.medtroniclabs.uhis.ncd.followup.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.setVisible
import org.medtroniclabs.uhis.databinding.FragmentNcdFollowUpSortDialogBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ncd.data.SortModelForFollowUp
import org.medtroniclabs.uhis.ncd.followup.NCDFollowUpUtils
import org.medtroniclabs.uhis.ncd.followup.viewmodel.NCDFollowUpViewModel

class NCDFollowUpSortDialog : DialogFragment() {
    private lateinit var binding: FragmentNcdFollowUpSortDialogBinding
    private val viewModel: NCDFollowUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNcdFollowUpSortDialogBinding.inflate(inflater, container, false)
        isCancelable = false
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    companion object {
        const val TAG = "NCDFollowUpSortDialog"

        fun newInstance() =
            NCDFollowUpSortDialog().apply {
            }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initListeners()
    }

    private fun initUI() {
        prefillValues()
    }

    private fun prefillValues() {
        viewModel.sortModel?.let { sort ->
            binding.apply {
                when {
                    sort.isScreeningDueDate == true -> rbScreeningLatestDueDate.isChecked = true
                    sort.isAssessmentDueDate == true -> rbAssessmentOldestDueDate.isChecked = true
                    sort.isMedicalReviewDueDate == true -> rbMrOldestDueDate.isChecked = true
                }
                enableResetBtn(
                    rbScreeningLatestDueDate.isChecked ||
                        rbAssessmentOldestDueDate.isChecked ||
                        rbMrOldestDueDate.isChecked,
                )
            }
        } ?: run {
            viewModel.sortModel = SortModelForFollowUp()
            binding.rgSortCondition.clearCheck()
        }
    }

    private fun enableResetBtn(isReset: Boolean) {
        binding.btnReset.isEnabled = isReset
    }

    private fun enableDoneBtn() {
        binding.btnDone.isEnabled =
            viewModel.sortModel != null &&
            (
                viewModel.sortModel?.isScreeningDueDate == true ||
                    viewModel.sortModel?.isAssessmentDueDate == true ||
                    viewModel.sortModel?.isMedicalReviewDueDate == true
            )
    }

    private fun initListeners() {
        enableDoneBtn()
        binding.apply {
            rbScreeningLatestDueDate.setVisible(viewModel.typeOffline == NCDFollowUpUtils.SCREENED)
            rbAssessmentOldestDueDate.setVisible(
                viewModel.typeOffline in listOf(NCDFollowUpUtils.Assessment_Type, NCDFollowUpUtils.LTFU_Type),
            )
            rbMrOldestDueDate.setVisible(
                viewModel.typeOffline in listOf(NCDFollowUpUtils.Defaulters_Type, NCDFollowUpUtils.LTFU_Type),
            )
            rgSortCondition.setOnCheckedChangeListener { _, checkedId ->
                enableResetBtn(true)
                viewModel.sortModel?.apply {
                    isScreeningDueDate = checkedId == R.id.rbScreeningLatestDueDate
                    isAssessmentDueDate = checkedId == R.id.rbAssessmentOldestDueDate
                    isMedicalReviewDueDate = checkedId == R.id.rbMrOldestDueDate
                }
                enableDoneBtn()
            }
        }

        binding.labelHeader.ivClose.safeClickListener {
            dismiss()
        }

        binding.btnDone.safeClickListener {
            viewModel.sortTriple()
            dismiss()
        }

        binding.btnReset.safeClickListener {
            doReset()
            viewModel.sortTriple()
            dismiss()
        }
    }

    private fun doReset() {
        viewModel.sortModel = null
        binding.rgSortCondition.clearCheck()
        binding.btnDone.isEnabled = false
        enableResetBtn(false)
    }
}
