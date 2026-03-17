package org.medtroniclabs.uhis.ui.dialog

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.setVisible
import org.medtroniclabs.uhis.appextensions.setWidth
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.databinding.FragmentMedicalReviewSucessDialogBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ui.landing.OnDialogDismissListener
import org.medtroniclabs.uhis.ui.mypatients.viewmodel.PatientDetailViewModel

class MedicalReviewSuccessDialogFragment : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentMedicalReviewSucessDialogBinding
    private var onDismissListener: OnDialogDismissListener? = null

    private val viewModel: PatientDetailViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onDismissListener = context as? OnDialogDismissListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMedicalReviewSucessDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "MedicalReviewSuccessDialogFragment"
        const val ISENROLLED = "ISENROLLED"

        fun newInstance(isEnroll: Boolean = false) =
            MedicalReviewSuccessDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ISENROLLED, isEnroll)
                }
            }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initializeListeners()
    }

    private fun getIsEnroll(): Boolean = arguments?.getBoolean(ISENROLLED) ?: false

    private fun initializeListeners() {
        viewModel.setUserJourney(AnalyticsDefinedParams.MEDICALREVIEWCOMPLETEDSUCCESSDIALOGUE)
        binding.btnEnroll.setVisible(CommonUtils.isNonCommunity() && (!getIsEnroll()))
        binding.btnDone.safeClickListener(this)
        binding.ivClose.safeClickListener(this)
        binding.btnEnroll.safeClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnDone.id, binding.ivClose.id -> {
                onDismissListener?.onDialogDismissListener()
                dismiss()
            }
            binding.btnEnroll.id -> {
                onDismissListener?.onDialogDismissListener(true)
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
