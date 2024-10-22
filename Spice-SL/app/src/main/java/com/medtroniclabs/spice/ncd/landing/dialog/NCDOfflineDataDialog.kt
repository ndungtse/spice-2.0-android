package com.medtroniclabs.spice.ncd.landing.dialog


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.databinding.FragmentNcdOfflineDataDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.landing.viewmodel.NCDOfflineDataViewModel
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDOfflineDataDialog : DialogFragment(), View.OnClickListener {

    private var onDismissListener: OnDialogDismissListener? = null
    private val viewModel: NCDOfflineDataViewModel by viewModels()
    private lateinit var binding: FragmentNcdOfflineDataDialogBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onDismissListener = context as OnDialogDismissListener
    }

    companion object {
        const val TAG = "NCDOfflineDataDialog"
        fun newInstance(): NCDOfflineDataDialog {
            return NCDOfflineDataDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdOfflineDataDialogBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.screeningCount.observe(viewLifecycleOwner) {
            handleScreening()

        }
        viewModel.assessmentType.observe(viewLifecycleOwner) {
            handleScreening()
        }
    }

    private fun handleScreening() {
        val screening = viewModel.screeningCount.value ?: 0
        val assessment = viewModel.assessmentType.value ?: 0
        binding.apply {
            btnUpload.setVisible(screening > 0 || assessment > 0)
            btnOkay.setVisible(screening <= 0 && assessment <= 0)
            screeningTitle.visible()
            assessmentGroup.visible()
        }
        offlineDataHandling(screening, assessment)
    }

    private fun offlineDataHandling(screening: Long, assessment: Long) {
        if (screening > 0) {
            binding.tvMessage.text =
                if (screening > 1) getString(
                    R.string.screened_patients,
                    screening.toString()
                ) else getString(
                    R.string.screened_patient
                )
        } else
            binding.tvMessage.text = getString(R.string.no_screened_patients)

        if (assessment > 0) {
            binding.tvAssessmentMessage.text =
                if (assessment > 1) getString(
                    R.string.assessed_patients,
                    assessment.toString()
                ) else getString(
                    R.string.assessed_patient
                )
        } else
            binding.tvAssessmentMessage.text = getString(R.string.no_assessed_patients)
    }

    private fun initView() {
        viewModel.getCountOfflineData()
        binding.screeningTitle.gone()
        binding.btnUpload.gone()
        binding.btnOkay.visible()
        binding.assessmentGroup.gone()
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnOkay.safeClickListener(this)
        binding.btnUpload.safeClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.labelHeader.ivClose.id, binding.btnCancel.id, binding.btnOkay.id -> {
                onDismissListener?.onDialogDismissListener()
                dismiss()
            }

            R.id.btnUpload -> {
                onDismissListener?.onDialogDismissListener(true)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDismissListener = null
    }
}