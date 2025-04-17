package com.medtroniclabs.spice.ui.household

import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams.FhirMemberID
import com.medtroniclabs.spice.common.DefinedParams.MemberID
import com.medtroniclabs.spice.common.DefinedParams.VillageId
import com.medtroniclabs.spice.common.DefinedParams.isCreateHouseholdForPhu
import com.medtroniclabs.spice.common.DefinedParams.isMemberRegistration
import com.medtroniclabs.spice.databinding.ActivityConsentFormBinding
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.isPhuWalkInsFlow
import com.medtroniclabs.spice.ui.household.fragment.ConsentSignatureDialogFragment
import com.medtroniclabs.spice.ui.household.viewmodel.ConsentFormViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConsentFormActivity : BaseActivity() {

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

    private fun initView() {

        viewModel.setUserJourney(getString(R.string.terms_and_condition))

        binding.btnSignature.setOnClickListener {
            val dialog = ConsentSignatureDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(VillageId, intent.getLongExtra(VillageId, -1L))
                    putBoolean(isPhuWalkInsFlow, intent.getBooleanExtra(isPhuWalkInsFlow, false))
                    putBoolean(isCreateHouseholdForPhu, intent.getBooleanExtra(isCreateHouseholdForPhu, false))
                    putLong(MemberID, intent.getLongExtra(MemberID, -1L))
                    putLong(FhirMemberID, intent.getLongExtra(FhirMemberID, -1L))
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
}