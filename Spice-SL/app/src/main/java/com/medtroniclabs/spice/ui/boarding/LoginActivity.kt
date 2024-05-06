package com.medtroniclabs.spice.ui.boarding

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.hideKeyboard
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.EncryptionUtil
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.Validator
import com.medtroniclabs.spice.databinding.ActivityLoginBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.boarding.viewmodel.LoginViewModel
import com.medtroniclabs.spice.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setMainContentView(binding.root)
        initView()
        setListeners()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.loginResponseLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState?.data?.let {
                        triggerResourceLoading()
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        showErrorSnackBar(it)
                    }
                }
            }
        }

    }

    private fun triggerResourceLoading() {
        startAsNewActivity(
            Intent(
                this@LoginActivity,
                ResourceLoadingScreen::class.java
            )
        )
    }

    private fun handleOfflineLoginSuccess() {
        SecuredPreference.putBoolean(
            SecuredPreference.EnvironmentKey.ISOFFLINELOGIN.name,
            true
        )
        startAsNewActivity(
            Intent(
                this@LoginActivity,
                LandingActivity::class.java
            )
        )
    }

    private fun setListeners() {
        binding.btnLogin.safeClickListener(this)
        binding.tvForgotPassword.safeClickListener(this)
    }

    private fun initView() {
        binding.tvUserNameLabel.markMandatory()
        binding.tvPasswordLabel.markMandatory()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLogin -> {
                hideKeyboard(view)
                validateLoginInputs()
            }

            R.id.tvForgotPassword -> {

            }
        }
    }

    private fun validateLoginInputs() {
        val userName = binding.userName.text.toString().trim()
        val password = binding.password.text.toString().trim()

        var isValid: Boolean

        if (userName.isBlank()) {
            isValid = false
            binding.tvUserEmailError.visibility = View.VISIBLE
            binding.tvUserEmailError.text = getString(R.string.email_cannot_be_empty)
        } else {
            isValid = validateEmailPhoneInput(userName)
        }

        if (isValid && password.isBlank()) {
            isValid = false
            binding.tvUserEmailError.visibility = View.GONE
            binding.tvUserPasswordError.visibility = View.VISIBLE
            binding.tvUserPasswordError.text = getString(R.string.password_cannot_be_empty)
        }

        val oldUserName =
            SecuredPreference.getString(SecuredPreference.EnvironmentKey.USERNAME.name)
        if (oldUserName != null && oldUserName != userName) {
            isValid = false
            showErrorDialogue(
                getString(R.string.warning_different_login_title),
                getString(R.string.warning_different_login_message, oldUserName)
            ) {

            }
        }

        if (isValid) {
            binding.tvUserEmailError.visibility = View.GONE
            binding.tvUserPasswordError.visibility = View.GONE
            if (!connectivityManager.isNetworkAvailable()) {
                showErrorSnackBar(getString(R.string.no_internet_error))
                val isToShowAlert = (userName == SecuredPreference.getString(
                    SecuredPreference.EnvironmentKey.USERNAME.name
                ) && EncryptionUtil.getSecurePassword(
                    password
                ) == SecuredPreference.getString(
                    SecuredPreference.EnvironmentKey.PASSWORD.name
                ))
                if (isToShowAlert && CommonUtils.isChw()) {
                    showErrorDialogue(
                        getString(R.string.alert),
                        message = getString(R.string.offline_login_message),
                        isNegativeButtonNeed = true
                    ) { buttonState ->
                        if (buttonState) {
                            handleOfflineLoginSuccess()
                        }
                    }
                }

            } else {
                viewModel.doLogin(userName, password)
            }

        }
    }

    private fun validateEmailPhoneInput(userName: String): Boolean {
        var isValid = true
        if (userName.contains(DefinedParams.AT_CHAR)) {
            if (!Validator.isEmailValid(userName)) {
                isValid = false
                binding.tvUserEmailError.visibility = View.VISIBLE
                binding.tvUserEmailError.text = getString(R.string.email_phone_invalid)
            }
        } else {
            if (!(Validator.isValidMobileNumber(userName))) {
                isValid = false
                binding.tvUserEmailError.visibility = View.VISIBLE
                binding.tvUserEmailError.text = getString(R.string.email_phone_invalid)
            }
        }
        return isValid
    }
}