package com.medtroniclabs.spice.ui.boarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.appextensions.cancelAllWorker
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityForgetPasswordBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.boarding.fragment.ConfirmPasswordFragment
import com.medtroniclabs.spice.ui.boarding.fragment.ResetPasswordFragment
import com.medtroniclabs.spice.ui.boarding.viewmodel.ForgotPasswordViewModel
import com.medtroniclabs.spice.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class ForgetPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setMainContentView(binding.root)

        //Get token from Deeplink. If token null -> Request reset password link.
        // If token not null -> Change password page will appear
        val token = intent?.data?.getQueryParameter("token")
        viewModel.updateResetToken(token)

        attachObservers()
    }

    private fun attachObservers() {

        viewModel.resetTokenLiveData.observe(this) {
            if (!it.isNullOrEmpty()) {
                supportFragmentManager.beginTransaction()
                    .replace(binding.fcEmailFragment.id, ConfirmPasswordFragment()).commit()

                Handler(Looper.getMainLooper()).postDelayed({
                    if (connectivityManager.isNetworkAvailable()) {
                        viewModel.validateToken(it)
                    } else {
                        showErrorDialogue(getString(R.string.error), getString(R.string.no_internet_error)) { isPositive ->
                            if (isPositive) {
                                redirectToLogin()
                            }
                        }
                    }
                }, 500)
            } else {
                supportFragmentManager.beginTransaction()
                    .add(binding.fcEmailFragment.id, ResetPasswordFragment()).commit()
            }
        }

        viewModel.resetEmailResponseLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = resource.message
                            ?: getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok)
                    ) {}
                }

                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.reset_password),
                        message = getString(R.string.email_registered_successfully),
                        positiveButtonName = getString(R.string.ok)
                    ) { isPositive ->
                        if (isPositive) {
                            redirectToLogin()
                        }
                    }
                }
            }
        }

        viewModel.verifyTokenLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = resource.message
                            ?: getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok)
                    ) {
                        finish()
                    }
                }

                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                }
            }
        }

        viewModel.resetPasswordLiveData.observe(this) {
            when (it.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = it.message ?: getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {}
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.reset_password),
                        message = getString(R.string.password_registered_successfully),
                        positiveButtonName = getString(R.string.ok)
                    ) {
                        redirectToLogin()

                    }
                }
            }
        }

    }

    fun redirectToLogin() {
        if (SecuredPreference.logout()) {
            cancelAllWorker()
            val intent = Intent(this, LandingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            UserDetail.referenceId = UUID.randomUUID().toString()
        }
    }
}