package org.medtroniclabs.uhis.ncd.landing.ui

import android.content.Intent
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
import androidx.activity.viewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.appextensions.cancelAllWorker
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.databinding.ActivityUserTermsConditionsBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ncd.landing.viewmodel.UserTermsConditionsViewModel
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.boarding.LoginActivity
import org.medtroniclabs.uhis.ui.landing.LandingActivity
import java.util.UUID

class UserTermsConditionsActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityUserTermsConditionsBinding

    private val viewModel: UserTermsConditionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserTermsConditionsBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.terms_and_condition),
            homeAndBackVisibility = Pair(false, false),
        )
        viewModel.getConsentForm(DefinedParams.Landing)
        initializeViews()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.updateTermsAndConditionsStatusLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    SecuredPreference.putBoolean(
                        SecuredPreference.EnvironmentKey.IS_TERMS_AND_CONDITIONS_APPROVED.name,
                        true,
                    )
                    startActivity(Intent(this, LandingActivity::class.java))
                    finish()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
        viewModel.consentEntityLiveData.observe(this) {
            loadRespectiveWebpage(it)
        }
    }

    private fun loadRespectiveWebpage(url: String) {
        binding.termsConditionWebView.loadDataWithBaseURL(null, url, "text/html", "utf-8", null)
    }

    private fun initializeViews() {
        binding.termsConditionWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
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

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: Bitmap?,
            ) {
                super.onPageStarted(view, url, favicon)
                showLoading()
            }

            override fun onPageFinished(
                view: WebView?,
                url: String?,
            ) {
                super.onPageFinished(view, url)
                hideLoading()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                super.onReceivedError(view, request, error)
                hideLoading()
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?,
            ) {
                hideLoading()
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?,
            ) {
                super.onReceivedSslError(view, handler, error)
                hideLoading()
            }
        }
        binding.termsConditionWebView.settings.javaScriptEnabled = true
        binding.termsConditionWebView.settings.apply {
            builtInZoomControls = false
            setSupportZoom(false)
        }
        binding.btnAccept.safeClickListener(this)
        binding.btnDecline.safeClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnDecline -> {
                if (SecuredPreference.logout()) {
                    cancelAllWorker()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    UserDetail.referenceId = UUID.randomUUID().toString()
                }
            }

            R.id.btnAccept -> {
                if (connectivityManager.isNetworkAvailable()) {
                    viewModel.updateTermsAndConditionsStatus(true)
                } else {
                    showErrorDialogue(
                        getString(R.string.error),
                        getString(R.string.no_internet_error),
                        isNegativeButtonNeed = false,
                    ) {}
                }
            }
        }
    }
}
