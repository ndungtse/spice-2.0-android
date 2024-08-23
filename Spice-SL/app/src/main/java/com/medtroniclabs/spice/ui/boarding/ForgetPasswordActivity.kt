package com.medtroniclabs.spice.ui.boarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityForgetPasswordBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.boarding.fragment.ConfirmPasswordFragment
import com.medtroniclabs.spice.ui.boarding.fragment.ResetPasswordFragment
import com.medtroniclabs.spice.ui.boarding.viewmodel.ForgotPasswordViewModel
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgetPasswordActivity : BaseActivity(), OnDialogDismissListener {

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
                viewModel.validateToken(it)
                supportFragmentManager.beginTransaction()
                    .replace(binding.fcEmailFragment.id, ConfirmPasswordFragment()).commit()
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
        finish()
        startAsNewActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        redirectToLogin()
    }
}