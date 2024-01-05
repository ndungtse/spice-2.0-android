package com.medtroniclabs.spice.ui.boarding

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.hideKeyboard
import com.medtroniclabs.spice.appextensions.markMandatory
import com.medtroniclabs.spice.appextensions.safeClickListener
import com.medtroniclabs.spice.common.Validator
import com.medtroniclabs.spice.formgenerator.definedproperties.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityLoginBinding
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.landing.LandingActivity

class LoginActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        setListeners()
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
        val userName = binding.userName.text
        val password = binding.password.text

        var isValid: Boolean

        if (userName.isNullOrBlank()) {
            isValid = false
            binding.tvUserEmailError.visibility = View.VISIBLE
            binding.tvUserEmailError.text = getString(R.string.email_cannot_be_empty)
        } else {
            isValid = validateEmailPhoneInput(userName)
        }

        if (isValid && password.isNullOrBlank()) {
            isValid = false
            binding.tvUserEmailError.visibility = View.GONE
            binding.tvUserPasswordError.visibility = View.VISIBLE
            binding.tvUserPasswordError.text = getString(R.string.password_cannot_be_empty)
        }

        if (isValid) {
            binding.tvUserEmailError.visibility = View.GONE
            binding.tvUserPasswordError.visibility = View.GONE
            doLogin(userName.toString(), password.toString())
        }
    }

    private fun validateEmailPhoneInput(userName: Editable): Boolean {
        var isValid = true
        if (userName.contains(DefinedParams.AT_CHAR)) {
            if (!Validator.isEmailValid(userName.toString())) {
                isValid = false
                binding.tvUserEmailError.visibility = View.VISIBLE
                binding.tvUserEmailError.text = getString(R.string.email_phone_invalid)
            }
        } else {
            if (!(Validator.isValidMobileNumber(userName.toString()))) {
                isValid = false
                binding.tvUserEmailError.visibility = View.VISIBLE
                binding.tvUserEmailError.text = getString(R.string.email_phone_invalid)
            }
        }
        return isValid
    }

    private fun doLogin(userName: String, password: String) {
        //TODO: API Call for login
        startActivity(Intent(this, LandingActivity::class.java))
    }
}