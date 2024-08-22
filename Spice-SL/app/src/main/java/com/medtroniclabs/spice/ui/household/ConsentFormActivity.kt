package com.medtroniclabs.spice.ui.household

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityConsentFormBinding
import com.medtroniclabs.spice.ui.BaseActivity
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
        binding.btnSignature.setOnClickListener {
            ConsentSignatureDialogFragment().show(supportFragmentManager, ConsentSignatureDialogFragment.TAG)
        }

        binding.wvTermAndCondition.isFocusable = false
        binding.wvTermAndCondition.isFocusableInTouchMode = false
        binding.wvTermAndCondition.webViewClient = webViewClientCallBack
        binding.wvTermAndCondition.settings.javaScriptEnabled = true
        binding.wvTermAndCondition.settings.setSupportMultipleWindows(true)
        binding.wvTermAndCondition.addJavascriptInterface(object {
            @JavascriptInterface
            fun openEmailClient(mailto: String) {
               Log.e("Test","afds")
            }
        }, "Android")

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
            return super.shouldOverrideUrlLoading(view, request)
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
    }
}