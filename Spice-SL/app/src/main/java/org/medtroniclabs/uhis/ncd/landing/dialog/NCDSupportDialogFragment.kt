package org.medtroniclabs.uhis.ncd.landing.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.databinding.NcdDialogSupportFragmentBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ui.landing.LandingActivity
import org.medtroniclabs.uhis.ui.landing.viewmodel.LandingViewModel

interface NCDSupportDialogListener {
    fun onSubmitClicked(message: String?)
}

class NCDSupportDialogFragment : DialogFragment(), View.OnClickListener {
    private var _binding: NcdDialogSupportFragmentBinding? = null
    private val binding: NcdDialogSupportFragmentBinding
        get() = _binding!!
    private var listener: NCDSupportDialogListener? = null
    private val viewModel: LandingViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as LandingActivity)
    }

    companion object {
        const val TAG = "NCDSupportDialogFragment"

        fun newInstance(): NCDSupportDialogFragment = NCDSupportDialogFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = NcdDialogSupportFragmentBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnSubmit.safeClickListener(this)
        binding.etSupportText.doAfterTextChanged { input ->
            input?.let {
                viewModel.enteredSupportReason =
                    if (it.trim().isNotEmpty()) it.trim().toString() else null
            }
            isEnableRefer()
        }
    }

    private fun isEnableRefer() {
        binding.btnSubmit.isEnabled = viewModel.enteredSupportReason != null
    }

    override fun onStart() {
        super.onStart()
        if (CommonUtils.checkIsTablet(requireContext())) {
            dialog?.window?.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
            )
        } else {
            dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener = null
        _binding = null
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.labelHeader.ivClose.id -> {
                dismiss()
            }

            R.id.btnSubmit -> {
                dismiss()
                listener?.onSubmitClicked(viewModel.enteredSupportReason)
            }
        }
    }
}
