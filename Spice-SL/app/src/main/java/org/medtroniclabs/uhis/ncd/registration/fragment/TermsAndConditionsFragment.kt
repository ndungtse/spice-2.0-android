package org.medtroniclabs.uhis.ncd.registration.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.databinding.FragmentTermsAndConditionsBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.mappingkey.Screening
import org.medtroniclabs.uhis.ncd.registration.ui.RegistrationActivity
import org.medtroniclabs.uhis.ncd.registration.viewmodel.TermsAndConditionsViewModel
import org.medtroniclabs.uhis.ncd.screening.fragment.ScreeningFormBuilderFragment
import org.medtroniclabs.uhis.ncd.screening.ui.ESignatureDialog
import org.medtroniclabs.uhis.ncd.screening.utils.SignatureInterface
import org.medtroniclabs.uhis.ui.BaseFragment

class TermsAndConditionsFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentTermsAndConditionsBinding
    private val viewModel: TermsAndConditionsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTermsAndConditionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        attachObservers()
    }

    private fun initViews() {
        if (isRegistration()) {
            binding.tvTermsAndConditionInfo.text =
                getString(R.string.terms_condition_info_enrollment)
            viewModel.getConsentForm(DefinedParams.Registration)
        } else {
            binding.tvTermsAndConditionInfo.text =
                getString(R.string.terms_condition_info_screening)
            viewModel.getConsentForm(DefinedParams.Screening)
        }

        binding.btnAccept.safeClickListener(this)
    }

    private fun attachObservers() {
        binding.etUserInitial.addTextChangedListener { patientInitial ->
            viewModel.patientInitial.value =
                if (patientInitial.isNullOrBlank()) null else patientInitial.trim().toString()
        }
        viewModel.consentEntityLiveData.observe(viewLifecycleOwner) {
            loadWebPage(it)
        }
        viewModel.patientInitial.observe(viewLifecycleOwner) { patientInitial ->
            binding.btnAccept.isEnabled = !patientInitial.isNullOrBlank()
        }
    }

    private fun loadWebPage(data: String) {
        binding.termsConditionWebView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnAccept -> {
                val eSignDialog = ESignatureDialog.newInstance(signatureInterface)
                eSignDialog.show(childFragmentManager, ESignatureDialog.TAG)
            }
        }
    }

    private val signatureInterface = object : SignatureInterface {
        override fun applySignature(
            signature: Bitmap?,
            initial: String?,
        ) {
            showProgress()
            val bundle = Bundle().apply {
                signature?.let { sign ->
                    putByteArray(
                        Screening.Signature,
                        CommonUtils.convertBitmapToByteArray(bitmap = sign),
                    )
                }
                putString(Screening.Initial, initial)
            }
            if (isRegistration()) {
                (activity as RegistrationActivity?)?.loadRegistrationFormFragment(bundle)
            } else {
                replaceFragmentIfExists<ScreeningFormBuilderFragment>(
                    R.id.screeningParentLayout,
                    bundle = bundle,
                    tag = ScreeningFormBuilderFragment.TAG,
                )
            }
        }
    }

    private fun isRegistration(): Boolean = arguments?.let { it.getString(FORM_TYPE)?.equals(DefinedParams.Registration) } == true

    companion object {
        const val FORM_TYPE = "FormType"
        const val TAG = "TermsAndConditionsFragment"
    }
}
