package com.medtroniclabs.spice.ui.medicalreview.addnewmember

import android.os.Bundle
import android.view.View
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.databinding.ActivityAddNewMemberBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.dialog.MedicalReviewSuccessDialogFragment
import com.medtroniclabs.spice.ui.dialog.SuccessDialogFragment
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.member.MemberRegistrationFragment

class AddNewMemberActivity : BaseActivity(), View.OnClickListener, OnDialogDismissListener {
    private lateinit var binding: ActivityAddNewMemberBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewMemberBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.member_registration),
            homeAndBackVisibility = Pair(true, true),
            callback = {
                backNavigation()
            },
            callbackHome = {
                backNavigationToHome()
            }
        )
        initializeView()
    }

    private fun initializeView() {
        binding.btnNext.text = getString(R.string.submit)
        binding.btnCancel.invisible()
        binding.btnNext.safeClickListener(this@AddNewMemberActivity)
        val args = Bundle().apply {
            putBoolean(MedicalReviewDefinedParams.MEDICAL_REVIEW_ADD_MEMBER, true)
        }
        replaceFragmentInId<MemberRegistrationFragment>(
            binding.fragmentContainer.id,
            bundle = args,
            tag = MemberRegistrationFragment::class.simpleName
        )
        supportFragmentManager.setFragmentResultListener(
            MedicalReviewDefinedParams.MEMBER_REG,
            this
        ) { _, _ ->
            SuccessDialogFragment.newInstance(
                householdNo = -1L,
                patientId = ""
            ).show(supportFragmentManager, SuccessDialogFragment.TAG)
        }
    }

    private fun backNavigation() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                onBackPressPopStack()
            }
        }
    }

    private fun backNavigationToHome() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                startActivityWithoutSplashScreen()
            }
        }
    }

    private fun onBackPressPopStack() {
        this@AddNewMemberActivity.finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnNext.id -> {
                val memberRegistrationFragment =
                    supportFragmentManager.findFragmentById(R.id.fragmentContainer) as MemberRegistrationFragment
                memberRegistrationFragment.medicalReviewAddMember(v)
            }
        }
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        startActivityWithoutSplashScreen()
    }
}