package com.medtroniclabs.spice.ncd.registration.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentTermsAndConditionsBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.screening.fragment.ScreeningFormBuilderFragment
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ncd.registration.ui.RegistrationActivity
import com.medtroniclabs.spice.ncd.registration.viewmodel.TermsAndConditionsViewModel
import com.medtroniclabs.spice.ncd.screening.ui.ESignatureDialog
import com.medtroniclabs.spice.ncd.screening.utils.SignatureInterface

class TermsAndConditionsFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentTermsAndConditionsBinding
    private val viewModel: TermsAndConditionsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTermsAndConditionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
        override fun applySignature(signature: Bitmap?, initial: String?) {
            val bundle = Bundle().apply {
                signature?.let { sign ->
                    putByteArray(
                        Screening.Signature,
                        CommonUtils.convertBitmapToByteArray(bitmap = sign)
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
                    tag = ScreeningFormBuilderFragment.TAG
                )
            }
        }
    }

    private fun isRegistration(): Boolean =
        arguments?.let { it.getString(FORM_TYPE)?.equals(DefinedParams.Registration) } == true

    companion object {
        const val FORM_TYPE = "FormType"
        const val TAG = "TermsAndConditionsFragment"
    }
}