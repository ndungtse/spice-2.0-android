package com.medtroniclabs.spice.ui.medicalreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.databinding.FragmentSignatureDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.signature.view.SignatureView

class SignatureDialogFragment() : DialogFragment(), View.OnClickListener {
    constructor(signatureListener: SignatureListener) : this() {
        this.signatureListener = signatureListener
    }

    private var signatureListener: SignatureListener? = null
    private lateinit var binding: FragmentSignatureDialogBinding

    private var isSigned: Boolean = false

    companion object {
        const val TAG = "SignatureDialogFragment"

        fun newInstance(signatureListener: SignatureListener): SignatureDialogFragment = SignatureDialogFragment(signatureListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignatureDialogBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        setListeners()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        )
    }

    private fun initializeView() {
        isCancelable = false
    }

    private fun setListeners() {
        binding.btnClearSign.safeClickListener(this)
        binding.clTitleCard.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnConfirm.safeClickListener(this)
        binding.signatureView.setOnSignedListener(signListener)
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnClearSign.id -> binding.signatureView.clear()
            binding.btnCancel.id -> dismiss()
            binding.clTitleCard.ivClose.id -> dismiss()
            binding.btnConfirm.id -> {
                if (isSigned()) {
                    signatureListener?.applySignature(binding.signatureView.getSignatureBitmap())
                    dismiss()
                }
            }
        }
    }

    private val signListener = object : SignatureView.OnSignedListener {
        override fun onStartSigning() {
            binding.tvErrorSignature.visibility = View.GONE
        }

        override fun onSigned() {
            isSigned = true
            binding.tvErrorSignature.visibility = View.GONE
        }

        override fun onClear() {
            isSigned = false
            binding.tvErrorSignature.visibility = View.GONE
        }
    }

    private fun isSigned(): Boolean {
        var signed = true
        if (!isSigned) {
            signed = false
            binding.tvErrorSignature.visibility = View.VISIBLE
        }
        return signed
    }
}
