package com.medtroniclabs.spice.ui.medicalreview.investigation.dialog

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.DialogConfirmationMarkAsReviewedBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener

class MarkAsReviewedConfirmationDialog(private val callback: (userConfirmed: Boolean) -> Unit) :
    DialogFragment(), View.OnClickListener {
    private lateinit var binding: DialogConfirmationMarkAsReviewedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogConfirmationMarkAsReviewedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()
    }

    private fun setClickListener() {
        binding.apply {
            btnCancel.safeClickListener(this@MarkAsReviewedConfirmationDialog)
            btnConfirm.safeClickListener(this@MarkAsReviewedConfirmationDialog)
            ivClose.safeClickListener(this@MarkAsReviewedConfirmationDialog)
        }
    }

    override fun onStart() {
        super.onStart()
        handleOrientation()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleOrientation()
    }

    private fun handleOrientation() {
        val isTablet = CommonUtils.checkIsTablet(requireContext())
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = when {
            isTablet && isLandscape -> 70
            else -> 100
        }
        val height = when {
            isTablet && isLandscape -> 35
            else -> 100
        }
        setDialogPercent(width, height)
    }

    companion object {
        const val TAG = "MarkAsReviewedConfirmationDialog"
        fun newInstance(callback: (userConfirmed: Boolean) -> Unit): MarkAsReviewedConfirmationDialog {
            return MarkAsReviewedConfirmationDialog(callback)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnCancel, R.id.ivClose -> dismiss()
            R.id.btnConfirm -> {
                dismiss()
                callback.invoke(true)
            }
        }
    }
}