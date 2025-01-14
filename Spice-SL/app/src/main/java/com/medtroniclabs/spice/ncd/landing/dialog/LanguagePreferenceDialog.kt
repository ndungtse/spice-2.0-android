package com.medtroniclabs.spice.ncd.landing.dialog

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.DialogLanguagePreferenceBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.landing.viewmodel.LanguagePreferenceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguagePreferenceDialog(private val listener: OnDialogDismissListener) : DialogFragment(),
    View.OnClickListener {
    private lateinit var binding: DialogLanguagePreferenceBinding
    private val viewModel: LanguagePreferenceViewModel by viewModels()

    companion object {
        const val TAG = "LanguagePreferenceDialog"
        fun newInstance(listener: OnDialogDismissListener): LanguagePreferenceDialog {
            val fragment = LanguagePreferenceDialog(listener)
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogLanguagePreferenceBinding.inflate(inflater, container, false)
        binding.btnConfirm.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getCultures()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.cultureList.observe(viewLifecycleOwner) { resource ->
            resource.data?.let {
                //
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleConfiguration()

    }

    override fun onStart() {
        super.onStart()
        handleConfiguration()
    }

    private fun handleConfiguration() {
        val isTablet = CommonUtils.checkIsTablet(requireContext())
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        when {
            isTablet && isLandscape -> setDialogPercent(50, 90)
            isTablet -> setDialogPercent(80, 50)
            else -> setDialogPercent(95, 65)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnCancel -> {
                dismiss()
            }

            R.id.btnConfirm -> {
                listener.onDialogDismissListener(true)
            }
        }
    }
}