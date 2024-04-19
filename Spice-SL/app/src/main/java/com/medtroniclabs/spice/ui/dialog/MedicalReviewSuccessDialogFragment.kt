package com.medtroniclabs.spice.ui.dialog

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.FragmentMedicalReviewSucessDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener

class MedicalReviewSuccessDialogFragment : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentMedicalReviewSucessDialogBinding
    private var onDismissListener: OnDialogDismissListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onDismissListener = context as OnDialogDismissListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMedicalReviewSucessDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }
    companion object {
        const val TAG = "MedicalReviewSuccessDialogFragment"
        fun newInstance() =
            MedicalReviewSuccessDialogFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeListeners()
    }

    private fun initializeListeners() {
        binding.btnDone.safeClickListener(this)
        binding.ivClose.safeClickListener(this)
    }

    override fun onClick(view: View) {
        when(view.id) {
            binding.btnDone.id, binding.ivClose.id -> {
                onDismissListener?.onDialogDismissListener()
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
            if (isLandscape) 50 else 60
        } else {
            if (isLandscape) 50 else 60
        }
        setWidth(width)
    }

}