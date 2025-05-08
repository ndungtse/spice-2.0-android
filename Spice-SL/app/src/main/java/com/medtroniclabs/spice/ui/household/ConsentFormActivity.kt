package com.medtroniclabs.spice.ui.household

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams.FhirMemberID
import com.medtroniclabs.spice.common.DefinedParams.HIV
import com.medtroniclabs.spice.common.DefinedParams.ID
import com.medtroniclabs.spice.common.DefinedParams.MemberID
import com.medtroniclabs.spice.common.DefinedParams.PatientId
import com.medtroniclabs.spice.common.DefinedParams.VillageId
import com.medtroniclabs.spice.common.DefinedParams.isCreateHouseholdForPhu
import com.medtroniclabs.spice.common.DefinedParams.isHouseHold
import com.medtroniclabs.spice.common.DefinedParams.isMemberRegistration
import com.medtroniclabs.spice.databinding.ActivityConsentFormBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.isPhuWalkInsFlow
import com.medtroniclabs.spice.ui.household.fragment.ConsentSignatureDialogFragment
import com.medtroniclabs.spice.ui.household.viewmodel.ConsentFormViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.activity.HivMedicalReviewBaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConsentFormActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityConsentFormBinding
    private val viewModel: ConsentFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConsentFormBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root, isToolbarVisible = true,
            title = getString(R.string.terms_and_condition),
            homeAndBackVisibility = Pair(true, true)
        )

        showLoading()
        initView()
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

    private fun initView() {
        if (intent.getBooleanExtra(HIV, false)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            binding.btnSignature.gone()
            binding.btnAccept.visible()
            viewModel.isHivFlow = intent.getBooleanExtra(HIV, false)
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            binding.btnAccept.gone()
            binding.btnSignature.visible()
        }
        viewModel.setUserJourney(getString(R.string.terms_and_condition))
        binding.btnSignature.setOnClickListener {
            val dialog = ConsentSignatureDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(VillageId, intent.getLongExtra(VillageId, -1L))
                    putBoolean(isPhuWalkInsFlow, intent.getBooleanExtra(isPhuWalkInsFlow, false))
                    putBoolean(isCreateHouseholdForPhu, intent.getBooleanExtra(isCreateHouseholdForPhu, false))
                    putBoolean(isHouseHold, intent.getBooleanExtra(isHouseHold, false))
                    putLong(MemberID, intent.getLongExtra(MemberID, -1L))
                    putLong(FhirMemberID, intent.getLongExtra(FhirMemberID, -1L))
                    putBoolean(
                        isMemberRegistration,
                        intent.getBooleanExtra(isMemberRegistration, false)
                    )
                }
            }
            dialog.show(supportFragmentManager, ConsentSignatureDialogFragment.TAG)
        }

        binding.wvTermAndCondition.webViewClient = webViewClientCallBack
        binding.wvTermAndCondition.settings.javaScriptEnabled = true
        binding.wvTermAndCondition.settings.apply {
            builtInZoomControls = false
            setSupportZoom(false)

        }

        viewModel.termsAndConditionStringLiveData.observe(this) {

            binding.wvTermAndCondition.loadDataWithBaseURL(
                null,
                it,
                "text/html",
                "UTF-8",
                null
            )

            binding.btnAccept.safeClickListener(this)
        }
    }

    private val webViewClientCallBack = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {

            request?.url?.let { emailUri ->
                if (emailUri.toString().startsWith("mailto")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, emailUri))
                        return true
                    } catch (e: Exception) {
                        return false
                    }
                } else {
                    showLoading()
                    view?.loadUrl(emailUri.toString())
                }
            }
            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            showLoading()
        }


        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            hideLoading()
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            hideLoading()
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            hideLoading()
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            super.onReceivedSslError(view, handler, error)
            hideLoading()
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnAccept -> {
                val intent = Intent(this, HivMedicalReviewBaseActivity::class.java).apply {
                    putExtra(PatientId, intent.getStringExtra(PatientId))
                    putExtra(HIV, intent.getBooleanExtra(HIV, false))
                    putExtra(ID, intent.getStringExtra(ID))
                    putExtra(MemberID, intent.getStringExtra(MemberID))
                }
                launcher.launch(intent)
            }
        }
    }

}