package org.medtroniclabs.uhis.ui.medicalreview.hiv.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.setDialogPercent
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.databinding.FragmentRecommendedInvestigationsDialogBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener

@AndroidEntryPoint
class RecommendedInvestigationsDialog : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentRecommendedInvestigationsDialogBinding

    var onOkayClickListener: (() -> Unit)? = null
    var onCancelClickListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRecommendedInvestigationsDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "RecommendedInvestigationsDialog"

        fun newInstance() = RecommendedInvestigationsDialog()
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
        setDialogPercent(width)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val blackText = getString(R.string.recommended_investigations_considering)
        val redText = getString(R.string.would_you_like_to_do_a_viral_load_test)

        val redColor = ContextCompat.getColor(requireContext(), R.color.a_red_error)

        val spannable = SpannableStringBuilder().apply {
            append(blackText)
            val start = length
            append(redText)
            setSpan(
                ForegroundColorSpan(redColor),
                start,
                length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        binding.tvSubTitle.text = spannable
        binding.tvSubTitle.text = spannable
        binding.btnOkay.isEnabled = true
        binding.btnOkay.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.ivClose.safeClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnOkay.id -> {
                onOkayClickListener?.invoke()
                dismiss()
            }
            binding.btnCancel.id, binding.ivClose.id -> {
                onCancelClickListener?.invoke()
                dismiss()
            }
        }
    }
}
